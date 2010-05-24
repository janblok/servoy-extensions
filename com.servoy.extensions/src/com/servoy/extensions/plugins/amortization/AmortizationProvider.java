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
package com.servoy.extensions.plugins.amortization;

import java.util.Calendar;
import java.util.Date;

import com.servoy.j2db.scripting.IScriptObject;

/**
 * @author sebster
 */
public class AmortizationProvider implements IScriptObject
{
	public String getSample(String methodName)
	{
		if ("newCalculation".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer buffer = new StringBuffer();

			buffer.append("// Calculate the interest rate for an amortization schedule\n");
			buffer.append("// with a loan of 2000 dollars  on January 1, 2005, and 5\n");
			buffer.append("// monthly payments of 500 dollars starting on Febuary 28th,\n");
			buffer.append("// payments made on the last day of each month.\n\n");
			buffer.append("// Get a new amortization calculation.\n");
			buffer.append("var c = plugins.amortization.newCalculation();\n\n");
			buffer.append("// Set the rate to -1 for unknown.\n");
			buffer.append("c.addRateChange(-1, new Date(2005, 0, 1));\n\n");
			buffer.append("// Set the compounding period to monthly.\n");
			buffer.append("c.addCompoundPeriodChange(12, new Date(2005, 0, 1));\n\n");
			buffer.append("// Add the loan and the payments to the schedule.\n");
			buffer.append("c.addLoan(2000, new Date(2005, 0, 1));\n");
			buffer.append("var lastDate = null;\n");
			buffer.append("var period = 12;\n");
			buffer.append("//valid periods are:");
			buffer.append("\n//PERIOD_ANNUALY : ");
			buffer.append(AmortizationCalculation.PERIOD_ANNUALY);
			buffer.append("\n//PERIOD_BI_ANNUALLY : ");
			buffer.append(AmortizationCalculation.PERIOD_BI_ANNUALLY);
// TODO: not supported yet...
//			buffer.append("\n//PERIOD_BI_MONTHLY : ");
//			buffer.append(AmortizationCalculation.PERIOD_BI_MONTHLY);
			buffer.append("\n//PERIOD_DAILY : ");
			buffer.append(AmortizationCalculation.PERIOD_DAILY);
			buffer.append("\n//PERIOD_FOUR_MONTHLY : ");
			buffer.append(AmortizationCalculation.PERIOD_FOUR_MONTHLY);
			buffer.append("\n//PERIOD_FOUR_WEEKLY : ");
			buffer.append(AmortizationCalculation.PERIOD_FOUR_WEEKLY);
			buffer.append("\n//PERIOD_MONTHLY : ");
			buffer.append(AmortizationCalculation.PERIOD_MONTHLY);
			buffer.append("\n//PERIOD_QUARTERLY : ");
			buffer.append(AmortizationCalculation.PERIOD_QUARTERLY);
			buffer.append("\n//PERIOD_TWO_MONTHLY : ");
			buffer.append(AmortizationCalculation.PERIOD_TWO_MONTHLY);
			buffer.append("\n//PERIOD_TWO_WEEKLY : ");
			buffer.append(AmortizationCalculation.PERIOD_TWO_WEEKLY);
			buffer.append("\n//PERIOD_WEEKLY : ");
			buffer.append(AmortizationCalculation.PERIOD_WEEKLY);
			buffer.append("\nvar number_count = 5;\n");
			buffer.append("var startday = 31;\n");
			buffer.append("c.addPayment(500, new Date(2005, 1, 28), lastDate, period, number_count, startday);\n\n");
			buffer.append("// Solve for the interest rate.\n");
			buffer.append("c.solveForUnknown();\n");
			buffer.append("// Get the interest rate and the error in the calculation.\n");
			buffer.append("// which should be small (otherwise the calculation did\n");
			buffer.append("// not converge for some reason.\n");
			buffer.append("var r = c.getUnknown();\n");
			buffer.append("var e = c.getError();\n\n");
			buffer.append("// When there are no unknowns you can calculate the\n");
			buffer.append("// actual amortization schedule.\n\n");
			buffer.append("// Same as before, use the calculated interest rate.\n");
			buffer.append("var c = plugins.amortization.newCalculation();\n");
			buffer.append("c.addRateChange(r, new Date(2005, 0, 1));\n");
			buffer.append("c.addCompoundPeriodChange(12, new Date(2005, 0, 1));\n");
			buffer.append("c.addLoan(2000, new Date(2005, 0, 1));\n");
			buffer.append("c.addPayment(500, new Date(2005, 1, 28), null, 12, 5, 31);\n\n");
			buffer.append("// Calculate the actual amortization schedule.\n");
			buffer.append("c.calculateAmortizationSchedule();\n\n");
			buffer.append("// Get the amortization schedule (which is a JSDataSet) and\n");
			buffer.append("// convert it to html. This way you can put it on a label.\n");
			buffer.append("// As a JSDataSet you can just get the values stored in\n");
			buffer.append("// the rows and columns to use in your script.\n");
			buffer.append("var s = \"<html>\" + c.getAmortizationSchedule().getAsHTML();\n\n");
			buffer.append("// Get the rest balance, which is the amount left over after\n");
			buffer.append("// the amortization schedule. In our case (since we calculated\n");
			buffer.append("// the rate to have nothing left, it should amount to no more\n");
			buffer.append("// than 1 or 2 cents due to rounding).\n");
			buffer.append("var rb = c.getRestBalance();\n");

			return buffer.toString();
		}
		else if ("nextDate".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer buffer = new StringBuffer();

			buffer.append("//Helper method to get the next date when a period is given;\n");
			buffer.append("//valid periods are:");
			buffer.append("\n//PERIOD_ANNUALY : ");
			buffer.append(AmortizationCalculation.PERIOD_ANNUALY);
			buffer.append("\n//PERIOD_BI_ANNUALLY : ");
			buffer.append(AmortizationCalculation.PERIOD_BI_ANNUALLY);
			buffer.append("\n//PERIOD_BI_MONTHLY : ");
// TODO: not supported yet...
//			buffer.append(AmortizationCalculation.PERIOD_BI_MONTHLY);
//			buffer.append("\n//PERIOD_DAILY : ");
			buffer.append(AmortizationCalculation.PERIOD_DAILY);
			buffer.append("\n//PERIOD_FOUR_MONTHLY : ");
			buffer.append(AmortizationCalculation.PERIOD_FOUR_MONTHLY);
			buffer.append("\n//PERIOD_FOUR_WEEKLY : ");
			buffer.append(AmortizationCalculation.PERIOD_FOUR_WEEKLY);
			buffer.append("\n//PERIOD_MONTHLY : ");
			buffer.append(AmortizationCalculation.PERIOD_MONTHLY);
			buffer.append("\n//PERIOD_QUARTERLY : ");
			buffer.append(AmortizationCalculation.PERIOD_QUARTERLY);
			buffer.append("\n//PERIOD_TWO_MONTHLY : ");
			buffer.append(AmortizationCalculation.PERIOD_TWO_MONTHLY);
			buffer.append("\n//PERIOD_TWO_WEEKLY : ");
			buffer.append(AmortizationCalculation.PERIOD_TWO_WEEKLY);
			buffer.append("\n//PERIOD_WEEKLY : ");
			buffer.append(AmortizationCalculation.PERIOD_WEEKLY);
			buffer.append("\n");
			buffer.append("var a_next_date = plugins.amortization.nextDate(new Date(2005, 1, 28),12,31);\n\n");

			buffer.append("//Sample to calculate intervals between 2 dates;\n");
			buffer.append("var startday = 31;\n");
			buffer.append("var d_start = new Date(2005, 1, 28)\n");
			buffer.append("var d_end = new Date(2005, 11, 20)\n");
			buffer.append("var allDates = new Array();\n");
			buffer.append("var next = d_start;\n");
			buffer.append("//loop over months\n");
			buffer.append("while (next.getTime() < d_end.getTime())\n");
			buffer.append("{\n");
			buffer.append("\tnext = plugins.amortization.nextDate(next,12,startday)\n");
			buffer.append("\tallDates[allDates.length] = next;\n");
			buffer.append("}\n");
			buffer.append("if (allDates.length > 0) allDates.length = allDates.length-1 //clear was one to far\n");
			buffer.append("//now check how many days are left\n");
			buffer.append("if (allDates.length > 0)\n");
			buffer.append("{\n");
			buffer.append("\tnext = allDates[allDates.length-1]\n");
			buffer.append("}\n");
			buffer.append("else\n");
			buffer.append("{\n");
			buffer.append("\tnext = d_start;\n");
			buffer.append("}\n");
			buffer.append("var days = 0;\n");
			buffer.append("//loop over days\n");
			buffer.append("while (next.getTime() < d_end.getTime() || next.getDate() < d_end.getDate())\n");
			buffer.append("{\n");
			buffer.append("\tnext = plugins.amortization.nextDate(next,365,startday)\n");
			buffer.append("\tdays++;\n");
			buffer.append("}\n");
			buffer.append("//add total days\n");
			buffer.append("allDates[allDates.length] = '(and) '+days+' days'\n");
			buffer.append("//make array textual to show\n");
			buffer.append("application.output( allDates.join('\\n') );\n");
			return buffer.toString();
		}
		else if ("newPolynomial".equals(methodName)) //$NON-NLS-1$
		{
			StringBuffer buffer = new StringBuffer();

			buffer.append("// create a new polynomial\n"); //$NON-NLS-1$
			buffer.append("var p = plugins.amortization.newPolynomial();\n\n"); //$NON-NLS-1$
			buffer.append("// make the polynomial -4*x^2 + 9\n"); //$NON-NLS-1$
			buffer.append("p.addTerm(-4, 2); // -4*x^2\n"); //$NON-NLS-1$
			buffer.append("p.addTerm(9, 0); // 9\n\n"); //$NON-NLS-1$
			buffer.append("// find the roots\n"); //$NON-NLS-1$
			buffer.append("var r1 = p.findRoot(1, 5E-15, 100);\n"); //$NON-NLS-1$
			buffer.append("var r2 = p.findRoot(-1, 5E-15, 100);\n\n"); //$NON-NLS-1$
			buffer.append("// get the derivative\n"); //$NON-NLS-1$
			buffer.append("q = p.getDerivative();\n\n"); //$NON-NLS-1$
			buffer.append("// show all this information in a dialog\n"); //$NON-NLS-1$
			buffer.append("plugins.dialogs.showInfoDialog(\n"); //$NON-NLS-1$
			buffer.append("\t\"polynomial\",\n"); //$NON-NLS-1$
			buffer.append("\t\"polynomial: \" + p + \"\\n\" +\n"); //$NON-NLS-1$
			buffer.append("\t\"derivative: \" + q + \"\\n\" +\n"); //$NON-NLS-1$
			buffer.append("\t\"value in 2: \" + p.getValue(2) + \"\\n\" +\n"); //$NON-NLS-1$
			buffer.append("\t\"derivative in 2: \" + p.getDerivativeValue(2) + \"\\n\" +\n"); //$NON-NLS-1$
			buffer.append("\t\"root near 1: \" + r1 + \"\\n\" +\n"); //$NON-NLS-1$
			buffer.append("\t\"root near -1: \" + r2,\n"); //$NON-NLS-1$
			buffer.append("\t\"Ok\"\n"); //$NON-NLS-1$
			buffer.append(");\n\n"); //$NON-NLS-1$
			buffer.append("// set q to zero\n"); //$NON-NLS-1$
			buffer.append("q.setToZero();\n\n"); //$NON-NLS-1$
			buffer.append("// make a copy of p, then add 1 and multiply by 3*x^2\n"); //$NON-NLS-1$
			buffer.append("var s = plugins.amortization.newPolynomial(p);\n"); //$NON-NLS-1$
			buffer.append("s.addTerm(1, 0); // add 1\n"); //$NON-NLS-1$
			buffer.append("s.multiplyByTerm(3, 2); // mulitply by 3*x^2\n\n"); //$NON-NLS-1$
			buffer.append("// make a copy of s and add p\n"); //$NON-NLS-1$
			buffer.append("var t = plugins.amortization.newPolynomial(s);\n"); //$NON-NLS-1$
			buffer.append("t.addPolynomial(p);\n\n"); //$NON-NLS-1$
			buffer.append("// make a copy of s and multiply by p\n"); //$NON-NLS-1$
			buffer.append("var u = plugins.amortization.newPolynomial(s);\n"); //$NON-NLS-1$
			buffer.append("u.multiplyByPolynomial(p);\n\n"); //$NON-NLS-1$
			buffer.append("// show all this information in a dialog\n"); //$NON-NLS-1$
			buffer.append("plugins.dialogs.showInfoDialog(\n"); //$NON-NLS-1$
			buffer.append("\t\"polynomial\",\n"); //$NON-NLS-1$
			buffer.append("\t\"polynomial: \" + p + \"\\n\" +\n"); //$NON-NLS-1$
			buffer.append("\t\"polynomial: \" + s + \"\\n\" +\n"); //$NON-NLS-1$
			buffer.append("\t\"sum: \" + t + \"\\n\" +\n"); //$NON-NLS-1$
			buffer.append("\t\"sum in 2: \" + p.getValue(2) + \" + \" + s.getValue(2) + \" = \" + t.getValue(2) + \"\\n\" +\n"); //$NON-NLS-1$
			buffer.append("\t\"product: \" + u + \"\\n\" +\n"); //$NON-NLS-1$
			buffer.append("\t\"product in 2: \" + p.getValue(2) + \" * \" + s.getValue(2) + \" = \" + u.getValue(2) + \"\\n\",\n"); //$NON-NLS-1$
			buffer.append("\t\"Ok\"\n"); //$NON-NLS-1$
			buffer.append(");\n"); //$NON-NLS-1$

			return buffer.toString();
		}
		return null;
	}

	public String getToolTip(String methodName)
	{
		if (methodName == null) return null;

		if ("newCalculation".equals(methodName)) //$NON-NLS-1$
		{
			return "Creates a new amortization calculation."; //$NON-NLS-1$
		}
		else if ("newPolynomial".equals(methodName)) //$NON-NLS-1$
		{
			return "Creates a new polynomial which is either 0 or a copy of the specified polynomial."; //$NON-NLS-1$
		}
		return null;
	}

	public String[] getParameterNames(String methodName)
	{
		if ("newPolynomial".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "[polynomial]" }; //$NON-NLS-1$
		}
		else if ("nextDate".equals(methodName)) //$NON-NLS-1$
		{
			return new String[] { "start_date", "period", "startday" }; //$NON-NLS-1$
		}
		return null;
	}

	public boolean isDeprecated(String methodName)
	{
		return false;
	}

	public Class[] getAllReturnedTypes()
	{
		return new Class[] { AmortizationCalculation.class, Polynomial.class };
	}

	/**
	 * Creates a new amortization calculation
	 *
	 * @sample
	 * // Calculate the interest rate for an amortization schedule
	 * // with a loan of 2000 dollars  on January 1, 2005, and 5
	 * // monthly payments of 500 dollars starting on Febuary 28th,
	 * // payments made on the last day of each month.
	 * 
	 * // Get a new amortization calculation.
	 * var c = plugins.amortization.newCalculation();
	 * 
	 * // Set the rate to -1 for unknown.
	 * c.addRateChange(-1, new Date(2005, 0, 1));
	 * 
	 * // Set the compounding period to monthly.
	 * c.addCompoundPeriodChange(12, new Date(2005, 0, 1));
	 * 
	 * // Add the loan and the payments to the schedule.
	 * c.addLoan(2000, new Date(2005, 0, 1));
	 * var lastDate = null;
	 * var period = 12;
	 * //valid periods are:
	 * //PERIOD_ANNUALY : 1
	 * //PERIOD_BI_ANNUALLY : 2
	 * //PERIOD_DAILY : 365
	 * //PERIOD_FOUR_MONTHLY : 3
	 * //PERIOD_FOUR_WEEKLY : 13
	 * //PERIOD_MONTHLY : 12
	 * //PERIOD_QUARTERLY : 4
	 * //PERIOD_TWO_MONTHLY : 6
	 * //PERIOD_TWO_WEEKLY : 26
	 * //PERIOD_WEEKLY : 52
	 * var number_count = 5;
	 * var startday = 31;
	 * c.addPayment(500, new Date(2005, 1, 28), lastDate, period, number_count, startday);
	 * 
	 * // Solve for the interest rate.
	 * c.solveForUnknown();
	 * // Get the interest rate and the error in the calculation.
	 * // which should be small (otherwise the calculation did
	 * // not converge for some reason.
	 * var r = c.getUnknown();
	 * var e = c.getError();
	 * 
	 * // When there are no unknowns you can calculate the
	 * // actual amortization schedule.
	 * 
	 * // Same as before, use the calculated interest rate.
	 * var c = plugins.amortization.newCalculation();
	 * c.addRateChange(r, new Date(2005, 0, 1));
	 * c.addCompoundPeriodChange(12, new Date(2005, 0, 1));
	 * c.addLoan(2000, new Date(2005, 0, 1));
	 * c.addPayment(500, new Date(2005, 1, 28), null, 12, 5, 31);
	 * 
	 * // Calculate the actual amortization schedule.
	 * c.calculateAmortizationSchedule();
	 * 
	 * // Get the amortization schedule (which is a JSDataSet) and
	 * // convert it to html. This way you can put it on a label.
	 * // As a JSDataSet you can just get the values stored in
	 * // the rows and columns to use in your script.
	 * var s = "<html>" + c.getAmortizationSchedule().getAsHTML();
	 * 
	 * // Get the rest balance, which is the amount left over after
	 * // the amortization schedule. In our case (since we calculated
	 * // the rate to have nothing left, it should amount to no more
	 * // than 1 or 2 cents due to rounding).
	 * var rb = c.getRestBalance();
	 */
	public AmortizationCalculation js_newCalculation()
	{
		return new AmortizationCalculation();
	}

	/**
	 * 
	 *
	 * @sample
	 * //Helper method to get the next date when a period is given;
	 * //valid periods are:
	 * //PERIOD_ANNUALY : 1
	 * //PERIOD_BI_ANNUALLY : 2
	 * //PERIOD_BI_MONTHLY : 365
	 * //PERIOD_FOUR_MONTHLY : 3
	 * //PERIOD_FOUR_WEEKLY : 13
	 * //PERIOD_MONTHLY : 12
	 * //PERIOD_QUARTERLY : 4
	 * //PERIOD_TWO_MONTHLY : 6
	 * //PERIOD_TWO_WEEKLY : 26
	 * //PERIOD_WEEKLY : 52
	 * var a_next_date = plugins.amortization.nextDate(new Date(2005, 1, 28),12,31);
	 * 
	 * //Sample to calculate intervals between 2 dates;
	 * var startday = 31;
	 * var d_start = new Date(2005, 1, 28)
	 * var d_end = new Date(2005, 11, 20)
	 * var allDates = new Array();
	 * var next = d_start;
	 * //loop over months
	 * while (next.getTime() < d_end.getTime())
	 * {
	 * 	next = plugins.amortization.nextDate(next,12,startday)
	 * 	allDates[allDates.length] = next;
	 * }
	 * if (allDates.length > 0) allDates.length = allDates.length-1 //clear was one to far
	 * //now check how many days are left
	 * if (allDates.length > 0)
	 * {
	 * 	next = allDates[allDates.length-1]
	 * }
	 * else
	 * {
	 * 	next = d_start;
	 * }
	 * var days = 0;
	 * //loop over days
	 * while (next.getTime() < d_end.getTime() || next.getDate() < d_end.getDate())
	 * {
	 * 	next = plugins.amortization.nextDate(next,365,startday)
	 * 	days++;
	 * }
	 * //add total days
	 * allDates[allDates.length] = '(and) '+days+' days'
	 * //make array textual to show
	 * application.output( allDates.join('\n') );
	 *
	 * @param start_date 
	 *
	 * @param period 
	 *
	 * @param startday 
	 */
	public Date js_nextDate(Date start_date, int period, int startday)
	{
		if (start_date == null) return null;

		Calendar c_org = Calendar.getInstance();
		c_org.setTime(start_date);
		Calendar c = AmortizationCalculation.nextCalendar(c_org, startday, period);
		return c.getTime();
	}

	/**
	 * Creates a new polynomial which is either 0 or a copy of the specified polynomial
	 *
	 * @sample
	 * // create a new polynomial
	 * var p = plugins.amortization.newPolynomial();
	 * 
	 * // make the polynomial -4*x^2 + 9
	 * p.addTerm(-4, 2); // -4*x^2
	 * p.addTerm(9, 0); // 9
	 * 
	 * // find the roots
	 * var r1 = p.findRoot(1, 5E-15, 100);
	 * var r2 = p.findRoot(-1, 5E-15, 100);
	 * 
	 * // get the derivative
	 * q = p.getDerivative();
	 * 
	 * // show all this information in a dialog
	 * plugins.dialogs.showInfoDialog(
	 * 	"polynomial",
	 * 	"polynomial: " + p + "\n" +
	 * 	"derivative: " + q + "\n" +
	 * 	"value in 2: " + p.getValue(2) + "\n" +
	 * 	"derivative in 2: " + p.getDerivativeValue(2) + "\n" +
	 * 	"root near 1: " + r1 + "\n" +
	 * 	"root near -1: " + r2,
	 * 	"Ok"
	 * );
	 * 
	 * // set q to zero
	 * q.setToZero();
	 * 
	 * // make a copy of p, then add 1 and multiply by 3*x^2
	 * var s = plugins.amortization.newPolynomial(p);
	 * s.addTerm(1, 0); // add 1
	 * s.multiplyByTerm(3, 2); // mulitply by 3*x^2
	 * 
	 * // make a copy of s and add p
	 * var t = plugins.amortization.newPolynomial(s);
	 * t.addPolynomial(p);
	 * 
	 * // make a copy of s and multiply by p
	 * var u = plugins.amortization.newPolynomial(s);
	 * u.multiplyByPolynomial(p);
	 * 
	 * // show all this information in a dialog
	 * plugins.dialogs.showInfoDialog(
	 * 	"polynomial",
	 * 	"polynomial: " + p + "\n" +
	 * 	"polynomial: " + s + "\n" +
	 * 	"sum: " + t + "\n" +
	 * 	"sum in 2: " + p.getValue(2) + " + " + s.getValue(2) + " = " + t.getValue(2) + "\n" +
	 * 	"product: " + u + "\n" +
	 * 	"product in 2: " + p.getValue(2) + " * " + s.getValue(2) + " = " + u.getValue(2) + "\n",
	 * 	"Ok"
	 * );
	 *
	 * @param [polynomial] optional 
	 */
	public Polynomial js_newPolynomial(Object[] arguments)
	{
		if (arguments.length == 0) return new Polynomial();
		else if (arguments.length == 1 && arguments[0] instanceof Polynomial) return new Polynomial((Polynomial)arguments[0]);

		return null;
	}
}
