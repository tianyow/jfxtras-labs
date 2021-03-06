/**
 * Copyright (c) 2011, JFXtras
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jfxtras.labs.scene.control;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.util.Callback;
import jfxtras.labs.scene.control.Agenda.Appointment;

/**
 * A textField with displays a calendar (date) with a icon to popup the CalendarPicker
 * The calendar is (and should) be treated as immutable. That means the setter is not used, but when a value is changed a new instance (clone) is put in the calendar property.
 * Features relative mutation options, like -1 or -1d for yesterday, -1m for minus one month, +1w, +2y. # is today.
 * 
 * To change the icon use:
 * .CalendarTextField .icon  {
 *     -fx-image: url("AlternateCalendarIcon.jpg");
 * }
 *
 * @author Tom Eugelink
 */
public class CalendarTextField extends Control
{
	// ==================================================================================================================
	// CONSTRUCTOR
	
	/**
	 * 
	 */
	public CalendarTextField()
	{
		construct();
	}
	
	/*
	 * 
	 */
	private void construct()
	{
		// setup the CSS
		// the -fx-skin attribute in the CSS sets which Skin class is used
		this.getStyleClass().add(this.getClass().getSimpleName());
		
		// this is apparently needed for good focus behavior
		setFocusTraversable(false);
		
		// construct properties
		constructShowTimeProperty();
	}

	/**
	 * Return the path to the CSS file so things are setup right
	 */
	@Override protected String getUserAgentStylesheet()
	{
		return this.getClass().getResource("/jfxtras/labs/internal/scene/control/" + this.getClass().getSimpleName() + ".css").toString();
	}
	
	// ==================================================================================================================
	// PROPERTIES
	
	/** Value: */
	public ObjectProperty<Calendar> valueProperty() { return valueObjectProperty; }
	final private ObjectProperty<Calendar> valueObjectProperty = new SimpleObjectProperty<Calendar>(this, "value", null);
	public Calendar getValue() { return valueObjectProperty.getValue(); }
	public void setValue(Calendar value) { valueObjectProperty.setValue(value); }
	public CalendarTextField withValue(Calendar value) { setValue(value); return this; }

	/** 
	 * The DateFormat used to render/parse the date in the textfield.
	 * The control uses the system default for date or datetime, and automatically switches between showing time or not depending on the ShowTime property.
	 * If the DateFormat property has been set manually, this behavior is disabled and the user is responsible for setting this correct DateFormat.     
	 */
	static private final DateFormat dateFormat = SimpleDateFormat.getDateInstance();
	static private final DateFormat dateTimeFormat = SimpleDateFormat.getDateTimeInstance();
	public ObjectProperty<DateFormat> dateFormatProperty() { return dateFormatObjectProperty; }
	final private ObjectProperty<DateFormat> dateFormatObjectProperty = new SimpleObjectProperty<DateFormat>(this, "dateFormat", dateFormat);
	public DateFormat getDateFormat() { return dateFormatObjectProperty.getValue(); }
	public void setDateFormat(DateFormat value) { dateFormatObjectProperty.setValue(value); }
	public CalendarTextField withDateFormat(DateFormat value) { setDateFormat(value); return this; }

	/** Locale: the locale is used to determine first-day-of-week, weekday labels, etc */
	public ObjectProperty<Locale> localeProperty() { return localeObjectProperty; }
	final private ObjectProperty<Locale> localeObjectProperty = new SimpleObjectProperty<Locale>(Locale.getDefault());
	public Locale getLocale() { return localeObjectProperty.getValue(); }
	public void setLocale(Locale value) { localeObjectProperty.setValue(value); }
	public CalendarTextField withLocale(Locale value) { setLocale(value); return this; } 
	
	/** PromptText: */
	public ObjectProperty<String> promptTextProperty() { return promptTextObjectProperty; }
	final private ObjectProperty<String> promptTextObjectProperty = new SimpleObjectProperty<String>(this, "promptText", null);
	public String getPromptText() { return promptTextObjectProperty.get(); }
	public void setPromptText(String value) { promptTextObjectProperty.set(value); }
	public CalendarTextField withPromptText(String value) { setPromptText(value); return this; }

	/** 
	 * ShowTime is used to setup the popup CalendarPicker correctly and also updates the DateFormat property accordingly.
	 * Once the DateFormat has been set manually, changes to ShowTime will no longer automatically update DateFormat.  
	 */
	public ObjectProperty<Boolean> showTimeProperty() { return showTimeObjectProperty; }
	volatile private ObjectProperty<Boolean> showTimeObjectProperty = new SimpleObjectProperty<Boolean>(this, "showTime", false);
	public Boolean getShowTime() { return showTimeObjectProperty.getValue(); }
	public void setShowTime(Boolean value) { showTimeObjectProperty.setValue(value); }
	public CalendarTextField withShowTime(Boolean value) { setShowTime(value); return (CalendarTextField)this; } 
	private void constructShowTimeProperty()
	{
		showTimeObjectProperty.addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue)
			{
				// change to show time
				if (newValue == true && getDateFormat() == dateFormat) setDateFormat(dateTimeFormat);
				if (newValue == false && getDateFormat() == dateTimeFormat) setDateFormat(dateFormat);
			}
		});
	}

	/** DateFormats: a list of alternate dateFormats used for parsing */
	public ListProperty<DateFormat> dateFormatsProperty() { return dateFormatsProperty; }
	ListProperty<DateFormat> dateFormatsProperty = new SimpleListProperty<DateFormat>(javafx.collections.FXCollections.observableList(new ArrayList<DateFormat>()));
	public ObservableList<DateFormat> getDateFormats() { return dateFormatsProperty.getValue(); }
	public void setDateFormats(ObservableList<DateFormat> value) { dateFormatsProperty.setValue(value); }
	public CalendarTextField withDateFormat(ObservableList<DateFormat> value) { setDateFormats(value); return this; }

	/** parse error callback:
	 * If something did not parse correctly, you may handle it. 
	 * Otherwise the exception will be logged on the console.
	 */
	public ObjectProperty<Callback<Throwable, Void>> parseErrorCallbackProperty() { return parseErrorCallbackObjectProperty; }
	final private ObjectProperty<Callback<Throwable, Void>> parseErrorCallbackObjectProperty = new SimpleObjectProperty<Callback<Throwable, Void>>(this, "parseErrorCallback", null);
	public Callback<Throwable, Void> getParseErrorCallback() { return this.parseErrorCallbackObjectProperty.getValue(); }
	public void setParseErrorCallback(Callback<Throwable, Void> value) { this.parseErrorCallbackObjectProperty.setValue(value); }
	public CalendarTextField withParseErrorCallback(Callback<Throwable, Void> value) { setParseErrorCallback(value); return this; }


	// ==================================================================================================================
	// EVENTS
	
	// ==================================================================================================================
	// BEHAVIOR
	
}
