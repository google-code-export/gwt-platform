package com.gwtplatform.mvp.client.proxy;

/**
 * Information sufficient to describe a text-only tab.
 * See {@link TabDescription} for more details.
 * 
 * @author Philippe Beaudoin
 */
public class TabDescriptionText implements TabDescription {

  private final String label;
  private final String historyToken;
  private final float priority;
  
  public TabDescriptionText( String label, String historyToken, float priority ) {
    this.label = label;
    this.historyToken = historyToken;
    this.priority = priority;
  }
  
  /**
   * Retrieves the text label to show on that tab.
   * 
   * @return The text label.
   */
  public String getLabel() {
    return label;
  }

  @Override
  public String getHistoryToken() {
    return historyToken;
  }

  @Override
  public float getPriority() {
    return priority;
  }
}
