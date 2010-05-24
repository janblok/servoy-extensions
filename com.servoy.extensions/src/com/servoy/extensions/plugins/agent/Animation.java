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
package com.servoy.extensions.plugins.agent;

import java.awt.Image;
import java.awt.MediaTracker;

/**
 * A class that represents an animation to be displayed
 * @author		jblok
 */
public class Animation
{
	private Image image;
	private MediaTracker tracker; // MediaTracker used to load images
	private AgentImpl owner; // Applet that contains this animation
	Animation(AgentImpl container, String namedImageFile, String namedSoundFile)
	{
		owner = container;
		tracker = new MediaTracker(owner);
		loadAnimationMedia(namedImageFile, namedSoundFile);
	}
	/**
	 * Loads the images and sounds involved with this animation
	 */
	void loadAnimationMedia(String file, String namedSoundFile)
	{
		//		frame.setSoundLocation(owner.getResource(Agent.soundsDir+"/"+file+i+".au"));
		image = owner.getImage(AgentImpl.imagesDir + "/" + file + ".gif"); //$NON-NLS-1$ //$NON-NLS-2$
		try
		{
			tracker.addImage(image, 0);
			/*		    if (soundTrackURL != null && soundTrack == null) 
						{
							soundTrack = new java.applet.Applet().getAudioClip(soundTrackURL);
							if (soundTrack == null) 
							{
					//		    owner.loadError(soundTrackURL, "soundtrack");
							    return;
							}
					    }
			
					    // Load the sounds into their frames
						for(int i = 0 ; i < frames.size() ; i++) 
						{
					        AnimationInfo frame = (AnimationInfo) frames.elementAt(i);
					        if (frame.getSoundLocation() != null) 
							{
					            try 
								{
					frame.setSound(new sun.applet.AppletAudioClip(frame.getSoundLocation()));
			/		frame.setSound(new java.applet.Applet().getAudioClip(frame.getSoundLocation()));
					            } 
								catch (Exception ex) 
								{
					                System.out.println(ex);
									ex.printStackTrace();
					            }
					        }
					    }
			*/
			try
			{
				tracker.waitForAll();
			}
			catch (InterruptedException e)
			{
			}
			/*
						offScrImage = owner.createImage(60,60);//owner.getSize().width, owner.getSize().height);
						System.out.println("offScrImage "+offScrImage);
					    offScrGC = offScrImage.getGraphics();
						System.out.println(offScrGC);
					    offScrGC.setColor(Color.lightGray);*/
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public Image getImage()
	{
		return image;
	}
	/**
	Image fetchImageAndWait(String imageURL, int trackerClass) throws InterruptedException 
	{
		Image image = owner.getImage(imageURL);
		tracker.addImage(image, trackerClass);
		tracker.waitForID(trackerClass);
		return image;
	}
	*/
}