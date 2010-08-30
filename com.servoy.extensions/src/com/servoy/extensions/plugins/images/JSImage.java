/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */
package com.servoy.extensions.plugins.images;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.mozilla.javascript.Wrapper;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.jpeg.JpegComponent;
import com.servoy.j2db.scripting.IScriptObject;
import com.servoy.j2db.util.ImageLoader;
import com.servoy.j2db.util.Utils;

/**
 * @author jcompagner
 */
public class JSImage implements IScriptObject, Wrapper
{

	private byte[] imageData;

	private Dimension dimension;

	private Map<String, Object[]> metadataMap;

	private final File file;

	// used by the javascript lib
	public JSImage()
	{
		file = null;
	}

	public JSImage(File file)
	{
		super();
		this.file = file;
	}

	public JSImage(byte[] image)
	{
		super();
		this.imageData = image;
		file = null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getSample(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String getSample(String methodName)
	{
		if ("resize".equals(methodName) || "getData".equals(methodName))
		{
			return "var image = plugins.images.getImage(byteArray_or_file_or_filename);//loads the image\n"
				+ "image = image.resize(200,200);//resizes it to 200,200\n" + "var bytes = image.getData();//gets the image bytes\n"
				+ "plugins.file.writeFile('filename',bytes);//saves the image bytes\n";
		}
		else if ("flip".equals(methodName))
		{
			return "var image = plugins.images.getImage(byteArray_or_file_or_filename);//loads the image\n" + "image = image.flip(0);//flip vertically\n"
				+ "var bytes = image.getData();//gets the image bytes\n" + "plugins.file.writeFile('filename',bytes);//saves the image bytes\n";
		}
		else if ("rotate".equals(methodName))
		{
			return "var image = plugins.images.getImage(byteArray_or_file_or_filename);//loads the image\n"
				+ "image = image.rotate(90);//rotate the image 90 degrees\n" + "var bytes = image.getData();//gets the image bytes\n"
				+ "plugins.file.writeFile('filename',bytes);//saves the image bytes\n";
		}
		else if (methodName.startsWith("getMetaData"))
		{
			return "var image = plugins.images.getImage(byteArray_or_file_or_filename);//loads the image\n"
				+ "// get the available metadata properties from the image, currently only jpg is supported\n"
				+ "var propertiesArray = image.getMetaDataProperties();\n" + "for(var i=0;i<propertiesArray.length;i++)\n" + "{\n"
				+ "	var property = propertiesArray[i]\n" + "	application.output(\"property: \" + property);\n"
				+ "	application.output(\"description (string): \" + image.getMetaDataDescription(property))\n"
				+ "	application.output(\"real object: \" + image.getMetaDataObject(property))\n" + "}\n"
				+ "// Thumbnail data is stored under property 'Exif - Thumbnail Data', extract that and set it in a dataprovider\n"
				+ "thumbnail = image.getMetaDataObject(\"Exif - Thumbnail Data\"); // gets thumbnail data from the image";
		}
		return "var image = plugins.images.getImage(byteArray_or_file);\nvar width = image.getWidth();\nvar height = image.getHeight();\nvar contentType = image.getContentType();\n";
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getToolTip(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String getToolTip(String methodName)
	{
		if ("getHeight".equals(methodName))
		{
			return "Gets the height of this image.";
		}
		else if ("getWidth".equals(methodName))
		{
			return "Gets the width of this image.";
		}
		else if ("getContentType".equals(methodName))
		{
			return "Gets the contenttype (image/jpeg) of this image.";
		}
		else if ("resize".equals(methodName))
		{
			return "Resizes the image to the width/height given, keeping aspect ratio. A new JSImage is returned.";
		}
		else if ("rotate".equals(methodName))
		{
			return "Rotates the image the number of degrees that is given. A new JSImage is returned.";
		}
		else if ("flip".equals(methodName))
		{
			return "Flips the image verticaly (type param=0) or horizontaly (type param=1). A new JSImage is returned.";
		}
		else if ("getData".equals(methodName))
		{
			return "Gets the bytes of this image, so that they can be saved to disk or stored the database.";
		}
		else if ("getMetaDataProperties".equals(methodName))
		{
			return "Gets the available metadata properties from the image. Currently only jpg is supported.";
		}
		else if ("getMetaDataDescription".equals(methodName))
		{
			return "Gets the description of a metadata property from the image. Currently only jpg is supported.";
		}
		else if ("getMetaDataObject".equals(methodName))
		{
			return "Gets the real object of a metadata property from the image. Currently only jpg is supported.";
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getParameterNames(java.lang.String)
	 */
	@SuppressWarnings("nls")
	public String[] getParameterNames(String methodName)
	{
		if ("resize".equals(methodName))
		{
			return new String[] { "width", "height" };
		}
		if ("rotate".equals(methodName))
		{
			return new String[] { "degrees" };
		}
		if ("flip".equals(methodName))
		{
			return new String[] { "type" };
		}
		return null;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#isDeprecated(java.lang.String)
	 */
	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	/**
	 * @see com.servoy.j2db.scripting.IScriptObject#getAllReturnedTypes()
	 */
	public Class< ? >[] getAllReturnedTypes()
	{
		return null;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return "JSImage[dimensions:" + getSize() + ",size:" + imageData.length + ",contenttype:" + js_getContentType() + "]";
	}

	private Dimension getSize()
	{
		if (dimension == null)
		{
			if (imageData != null)
			{
				dimension = ImageLoader.getSize(imageData);
			}
			else
			{
				dimension = ImageLoader.getSize(file);
			}
		}
		return dimension;
	}

	/**
	 * get the height of this image
	 *
	 * @sampleas js_getContentType()
	 */
	public int js_getHeight()
	{
		return getSize().height;
	}

	/**
	 * get the width of this image
	 *
	 * @sampleas js_getContentType()
	 */
	public int js_getWidth()
	{
		return getSize().width;
	}

	/**
	 * get the bytes of this image, so that it can be saved to disk or stored the database
	 *
	 * @sample
	 * var image = plugins.images.getImage(byteArray_or_file_or_filename);//loads the image
	 * image = image.resize(200,200);//resizes it to 200,200
	 * var bytes = image.getData();//gets the image bytes
	 * plugins.file.writeFile('filename',bytes);//saves the image bytes
	 */
	public byte[] js_getData()
	{
		if (imageData == null)
		{
			imageData = Utils.readFile(file, -1);
		}
		return imageData;
	}

	/**
	 * resizes the image to the width/height given, keeping aspect ratio, a new JSImage is returned
	 *
	 * @sampleas js_getData()
	 *
	 * @param width 
	 *
	 * @param height 
	 */
	public JSImage js_resize(int width, int height)
	{
		js_getData();
		byte[] array = ImageLoader.resize(imageData, width, height, true);
		if (array != null)
		{
			return new JSImage(array);
		}
		return null;
	}

	/**
	 * rotates the image the number of degrees that is given, a new JSImage is returned
	 *
	 * @sample
	 * var image = plugins.images.getImage(byteArray_or_file_or_filename);//loads the image
	 * image = image.rotate(90);//rotate the image 90 degrees
	 * var bytes = image.getData();//gets the image bytes
	 * plugins.file.writeFile('filename',bytes);//saves the image bytes
	 *
	 * @param degrees 
	 */
	public JSImage js_rotate(final double currentAngle)
	{
		js_getData();
		final double radians = Math.toRadians(currentAngle);

		final int currentWidth = getSize().width;
		final int currentHeight = getSize().height;

		final int newWidth = (int)(Math.abs((currentWidth * Math.cos(radians) + currentHeight * Math.sin(radians))) + 0.5);
		final int newHeight = (int)(Math.abs((currentWidth * Math.sin(radians) + currentHeight * Math.cos(radians))) + 0.5);

		BufferedImage image = ImageLoader.getBufferedImage(imageData, currentWidth, currentHeight, true);

		int type = image.getType();
		if (type == 0) type = BufferedImage.TYPE_INT_ARGB_PRE;

		BufferedImage bi = new BufferedImage(newWidth, newHeight, type);

		Graphics2D g2d = (Graphics2D)bi.getGraphics();
		if (image.getAlphaRaster() == null)
		{
			g2d.setColor(new Color(255, 255, 255, 255));
		}
		else
		{
			g2d.setColor(new Color(255, 255, 255, 0));
		}
		g2d.fillRect(0, 0, newWidth, newHeight);
		AffineTransform origXform = g2d.getTransform();
		AffineTransform newXform = (AffineTransform)(origXform.clone());
		// center of rotation is center of the panel
		int xRot = newWidth / 2;
		int yRot = newHeight / 2;

		newXform.rotate(radians, xRot, yRot);
		g2d.setTransform(newXform);
		// draw image centered in panel
		int x = (newWidth - currentWidth) / 2;
		int y = (newHeight - currentHeight) / 2;
		g2d.drawImage(image, x, y, null);
		g2d.setTransform(origXform);
		g2d.dispose();

		try
		{
			return new JSImage(ImageLoader.getByteArray(js_getContentType(), bi));
		}
		finally
		{
			bi.flush();
		}
	}

	/**
	 * flips the image vertical (type param=0) or horizontal (type param=1), a new JSImage is returned
	 *
	 * @sample
	 * var image = plugins.images.getImage(byteArray_or_file_or_filename);//loads the image
	 * image = image.flip(0);//flip vertically
	 * var bytes = image.getData();//gets the image bytes
	 * plugins.file.writeFile('filename',bytes);//saves the image bytes
	 *
	 * @param type 
	 */
	public JSImage js_flip(int type)
	{
		js_getData();
		final int currentWidth = getSize().width;
		final int currentHeight = getSize().height;

		BufferedImage bufferedImage = ImageLoader.getBufferedImage(imageData, currentWidth, currentHeight, true);

		if (type == 0)
		{
			//	 Flip the image vertically
			AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -bufferedImage.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			bufferedImage = op.filter(bufferedImage, null);
		}
		else
		{
			// Flip the image horizontally
			AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
			tx.translate(-bufferedImage.getWidth(null), 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			bufferedImage = op.filter(bufferedImage, null);
		}
		try
		{
			return new JSImage(ImageLoader.getByteArray(js_getContentType(), bufferedImage));
		}
		finally
		{
			bufferedImage.flush();
		}
	}

	/**
	 * get the contenttype (image/jpeg) of this image
	 *
	 * @sample
	 * var image = plugins.images.getImage(byteArray_or_file);
	 * var width = image.getWidth();
	 * var height = image.getHeight();
	 * var contentType = image.getContentType();
	 */
	public String js_getContentType()
	{
		if (imageData != null)
		{
			return ImageLoader.getContentType(imageData, file != null ? file.getName() : null);
		}
		else if (file != null)
		{
			byte[] bytes = Utils.readFile(file, 32);
			if (bytes != null)
			{
				return ImageLoader.getContentType(bytes, file.getName());
			}
		}
		return null;
	}


	/**
	 * @sameas js_getMetaDataDescription()
	 */
	public String[] js_getMetaDataProperties()
	{
		generateMetaData();
		return metadataMap.keySet().toArray(new String[metadataMap.size()]);
	}

	/**
	 * 
	 *
	 * @sample
	 * var image = plugins.images.getImage(byteArray_or_file_or_filename);//loads the image
	 * // get the available metadata properties from the image, currently only jpg is supported
	 * var propertiesArray = image.getMetaDataProperties();
	 * for(var i=0;i<propertiesArray.length;i++)
	 * {
	 * 	var property = propertiesArray[i]
	 * 	application.output("property: " + property);
	 * 	application.output("description (string): " + image.getMetaDataDescription(property))
	 * 	application.output("real object: " + image.getMetaDataObject(property))
	 * }
	 * // Thumbnail data is stored under property 'Exif - Thumbnail Data', extract that and set it in a dataprovider
	 * thumbnail = image.getMetaDataObject("Exif - Thumbnail Data"); // gets thumbnail data from the image
	 */
	public String js_getMetaDataDescription(String property)
	{
		generateMetaData();
		Object[] objects = metadataMap.get(property);
		if (objects != null)
		{
			return (String)objects[0];
		}
		return null;
	}

	/**
	 * @sameas js_getMetaDataDescription()
	 */
	public Object js_getMetaDataObject(String property)
	{
		generateMetaData();
		Object[] objects = metadataMap.get(property);
		if (objects != null)
		{
			return objects[1];
		}
		return null;
	}

	/**
	 * @see org.mozilla.javascript.Wrapper#unwrap()
	 */
	public Object unwrap()
	{
		return js_getData();
	}

	@SuppressWarnings({ "unchecked", "nls" })
	private void generateMetaData()
	{
		if (metadataMap != null) return;
		metadataMap = new TreeMap<String, Object[]>();
		if (imageData == null && file == null) return;
		try
		{

			Metadata metadata = null;
			if (imageData != null)
			{
				metadata = ImageMetadataReader.readMetadata(new BufferedInputStream(new ByteArrayInputStream(imageData), 512));
			}
			else
			{
				metadata = ImageMetadataReader.readMetadata(file);
			}
			Iterator<Directory> directories = metadata.getDirectoryIterator();
			while (directories.hasNext())
			{
				Directory directory = directories.next();
				// iterate through tags and print to System.out  
				Iterator<Tag> tags = directory.getTagIterator();
				while (tags.hasNext())
				{
					Tag tag = tags.next();
					// use Tag.toString()  
					if (tag.getDirectoryName().equals("Sony Makernote")) continue;
					String description = "";
					try
					{
						description = tag.getDescription();
						if (tag.getDescription() == null || "".equals(description)) continue;
					}
					catch (MetadataException ex)
					{
						continue;
					}
					String name = tag.getTagName();
					if (name.indexOf("Unknown tag") != -1) continue;

					Object object = directory.getObject(tag.getTagType());
					if (object instanceof JpegComponent || object instanceof int[] || (object instanceof byte[] && ((byte[])object).length <= 4))
					{
						object = description;
					}
					else if (object instanceof Rational)
					{
						object = ((Rational)object).toString();
					}
					metadataMap.put(tag.getDirectoryName() + " - " + name, new Object[] { description, object });
				}
			}
		}
		catch (ImageProcessingException jpe)
		{
			// ignore not a jpg or valid image for extracting exif.
		}
	}
}
