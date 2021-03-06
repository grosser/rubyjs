/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.user.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Element;

/**
 * The base class for the majority of user-interface objects. Widget adds
 * support for receiving events from the browser and being added directly to
 * {@link com.google.gwt.user.client.ui.Panel panels}.
 */
public class Widget extends UIObject implements EventListener {

  private boolean attached;
  private Object layoutData;
  private Widget parent;

  /**
   * Gets this widget's parent panel.
   * 
   * @return the widget's parent panel
   */
  public Widget getParent() {
    return parent;
  }

  /**
   * Determines whether this widget is currently attached to the browser's
   * document (i.e., there is an unbroken chain of widgets between this widget
   * and the underlying browser document).
   * 
   * @return <code>true</code> if the widget is attached
   */
  public boolean isAttached() {
    return attached;
  }

  public void onBrowserEvent(Event event) {
  }

  /**
   * Removes this widget from its parent widget. If it has no parent, this
   * method does nothing.
   * 
   * @throws IllegalStateException if this widget's parent does not support
   *           removal (e.g. {@link Composite})
   */
  public void removeFromParent() {
    if (parent instanceof HasWidgets) {
      ((HasWidgets) parent).remove(this);
    } else if (parent != null) {
      throw new IllegalStateException(
          "This widget's parent does not implement HasWidgets");
    }
  }

  /**
   * This method is called when a widget is attached to the browser's document.
   * To receive notification after a Widget has been added from the
   * document, override the {@link #onLoad} method.
   * 
   * <p>
   * Subclasses that override this method must call
   * <code>super.onAttach()</code> to ensure that the Widget has been
   * attached to the underlying Element.
   * </p>
   * 
   * @throws IllegalStateException if this widget is already attached
   */
  protected void onAttach() {
    if (attached) {
      throw new IllegalStateException(
          "Should only call onAttach when the widget is detached from the browser's document");
    }

    attached = true;

    // Set the main element's event listener. This should only be set
    // while the widget is attached, because it creates a circular
    // reference between JavaScript and the DOM.
    DOM.setEventListener(getElement(), this);

    // Now that the widget is attached, call onLoad().
    onLoad();
  }

  /**
   * This method is called when a widget is detached from the browser's
   * document. To receive notification before a Widget is removed from the
   * document, override the {@link #onUnload} method.
   * 
   * <p>
   * Subclasses that override this method must call
   * <code>super.onDetach()</code> to ensure that the Widget has been
   * detached from the underlying Element.  Failure to do so will result
   * in application memeroy leaks due to circular references between DOM
   * Elements and JavaScript objects.
   * </p>
   * 
   * @throws IllegalStateException if this widget is already detached
   */
  protected void onDetach() {
    if (!attached) {
      throw new IllegalStateException(
          "Should only call onDetach when the widget is attached to the browser's document");
    }
    
    // Give the user a chance to clean up, but don't trust the code to not throw
    try {
      onUnload();
    } finally {
      attached = false;
  
      // Clear out the element's event listener (breaking the circular
      // reference between it and the widget).
      DOM.setEventListener(getElement(), null);
    }
  }

  /**
   * This method is called immediately after a widget becomes attached to the
   * browser's document.
   */
  protected void onLoad() {
  }
  
  /**
   * This method is called immediately before a widget will be detached from the
   * browser's document.
   */
  protected void onUnload() {
  }

  /**
   * Sets this object's browser element. Widget subclasses must call this method
   * before attempting to call any other methods.
   * 
   * If a browser element has already been attached, then it is replaced with
   * the new element. The old event listeners are removed from the old browser
   * element, and the event listeners are set up on the new browser element.
   * 
   * @param elem the object's new element
   */
  protected void setElement(Element elem) {
    if (attached) {
      // Remove old event listener to avoid leaking. onDetach will not do this
      // for us, because it is only called when the widget itself is detached from
      // the document.
      DOM.setEventListener(getElement(), null);
    }

    super.setElement(elem);
    if (attached) {
      // Hook the event listener back up on the new element. onAttach will not
      // do this for us, because it is only called when the widget itself is
      // attached to the document.
      DOM.setEventListener(elem, this);
    }
  }

  /**
   * Gets the panel-defined layout data associated with this widget.
   * 
   * @return the widget's layout data
   * @see #setLayoutData
   */
  Object getLayoutData() {
    return layoutData;
  }

  /**
   * Sets the panel-defined layout data associated with this widget. Only the
   * panel that currently contains a widget should ever set this value. It
   * serves as a place to store layout bookkeeping data associated with a
   * widget.
   * 
   * @param layoutData the widget's layout data
   */
  void setLayoutData(Object layoutData) {
    this.layoutData = layoutData;
  }

  /**
   * Sets this widget's parent. This method should only be called by
   * {@link Panel} and {@link Composite}.
   * 
   * @param parent the widget's new parent
   */
  void setParent(Widget parent) {
    Widget oldParent = this.parent;
    this.parent = parent;
    if (parent == null) {
      if (oldParent != null && oldParent.isAttached()) {
        onDetach();
        assert !isAttached() :
            "Failure of " + GWT.getTypeName(this) + " to call super.onDetach()";
      }
    } else if (parent.isAttached()) {
      onAttach();
        assert isAttached() :
            "Failure of " + GWT.getTypeName(this) + " to call super.onAttach()";
    }
  }
}
