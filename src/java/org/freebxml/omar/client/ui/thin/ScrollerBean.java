/*
  * QueryPanelBean.java
  *
  * Created on April 4, 2004, 10:18 PM
  */

package org.freebxml.omar.client.ui.thin;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.xml.registry.JAXRException;
import javax.faces.component.UIData;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.FactoryFinder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.ui.thin.components.components.ScrollerComponent;
import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.common.IterativeQueryParams;

/**
  *
  * @author  psterk
  */
public class ScrollerBean implements Serializable {
    
    private static final Log log = LogFactory.getLog(ScrollerBean.class);
    private UIData data = null;
    private int currentRow = 0;
    private int nextRow = 0;
    private int totalResultCount = 0;
    
    public static int MIN_NUMBER_OF_SEARCH_RESULTS = 1;
    public static int DEFAULT_NUMBER_OF_SEARCH_RESULTS = 4;
    public static int MAX_NUMBER_OF_SEARCH_RESULTS = 50;

    public UIData getData() {
        return data;
    }

    public void setData(UIData data) {
        this.data = data;
    }


    // ---------------------------------------------------------- Action Methods
    /**
     * <p>Scroll directly to the first page.</p>
     */
    public String first() {
        scroll(0);
        return (null);
    }


    /**
     * <p>Scroll directly to the last page.</p>
     */
    public String last() {
        scroll(data.getRowCount() - 1);
        return (null);
    }


    /**
     * <p>Scroll forwards to the next page.</p>
     */
    public String next() {
        int first = data.getFirst();
        scroll(first + data.getRows());
        return (null);
    }


    /**
     * <p>Scroll backwards to the previous page.</p>
     */
    public String previous() {
        int first = data.getFirst();
        scroll(first - data.getRows());
        return (null);
    }


    /**
     * <p>Scroll to the page that contains the specified row number.</p>
     *
     * @param row Desired row number
     */
    public void scroll(int row) {
        int rows = data.getRows();
        if (rows < 1) {
            return; // Showing entire table already
        }
        if (row < 0) {
            data.setFirst(0);
        } else if (row >= data.getRowCount()) {
            data.setFirst(data.getRowCount() - 1);
        } else {
            int modulus = row % rows;
            int result = row - modulus;
            data.setFirst(result);
        }
    }


    /**
     * Handles the ActionEvent generated as a result of clicking on a
     * link that points a particular page in the result-set.
     */
    public void processScrollEvent(ActionEvent event) {
        if (log.isTraceEnabled()) {
            log.trace("TRACE: ResultSetBean.processScrollEvent ");
        }        
        if (data == null) {
            // If data == null, the ScrollerBean received a spurious ActionEvent
            // Ignore it.
            return;
        }
        UIComponent component = event.getComponent();
        Integer curRow = (Integer) component.getAttributes().get("currentRow");
        if (curRow != null) {
            int curRowInt = curRow.intValue();
            String lastAction = ((ScrollerComponent)component).getLastAction();
            int lastActionInt = new Integer(lastAction).intValue();
            if (lastActionInt == ScrollerComponent.ACTION_NEXT && 
                currentRow > curRowInt) {
                appendNewResults(curRowInt);
		setCurrentRow(component, data.getRows());
            } else if (lastActionInt == ScrollerComponent.ACTION_PREVIOUS && 
                (currentRow - curRowInt) > data.getRows()) {
                setCurrentRow(component, -(data.getRows()));
            } else {
                appendNewResults(curRowInt);
                currentRow = curRow.intValue();
            }
        }
        // scroll to the appropriate page in the ResultSet.
        scroll(currentRow);
    }
    
    private void appendNewResults(int curRowInt) {
        int totalRows = getTotalResultCount();
        // 1. What is the start index?
        int startIndex = data.getRowCount();
        if (totalRows == startIndex) {
            // We have all the rows
            return;
        } else {          
            // Calculate how many rows to append to the result set
            int maxResults = -1;
            int numResultsPerPage = getNumberOfSearchResults();
            // 2. What is the max results?
            int rowsLeft = totalRows - startIndex;
            int rowsToGet = (curRowInt - currentRow) + 1;
            if (rowsLeft < rowsToGet){
                maxResults = rowsLeft;
            } else {
                maxResults = rowsToGet;
            }
            /*
            int modulo = maxResults % numResultsPerPage;
            if (modulo > 0) {
                maxResults = maxResults + (numResultsPerPage - modulo);
            }
             */
            if (maxResults > 0) {
                // 3. Create IterativeQueryParams with start index and max results
                IterativeQueryParams iqParams = new IterativeQueryParams(startIndex, maxResults);
                try {
                    // 4. Do query and append results
                    SearchPanelBean.getInstance().doSearchAppendResultsToResultSet(iqParams);
                } catch (Exception ex) {
                    String message = WebUIResourceBundle.getInstance()
                                                          .getString("appendResultsError") + 
                                                              ex.getLocalizedMessage();
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        message, null));
                }
            }
        }
    }
    
    private void setCurrentRow(UIComponent component, int rowAdjust) {
        FacesContext context = FacesContext.getCurrentInstance();
        int currentPage = ((ScrollerComponent)component).getCurrentPage(context);
        component.getAttributes().put("currentPage", new Integer(currentPage));
        log.warn(WebUIResourceBundle.getInstance().getString("message.scrollerComponentHasIncorrectState"));
        currentRow = currentRow + rowAdjust;
    }
    
    public int getCurrentRow() {
        return currentRow;   
    }

    public int getNextRow() {
        int numSearchResults = getNumberOfSearchResults();
        nextRow = numSearchResults + currentRow;
        if ((numSearchResults + currentRow) > totalResultCount){
            nextRow = totalResultCount;
        }
      return nextRow;
    }

    public int getNumberOfSearchResults(){
        String numberOfSearchResults = null;
        numberOfSearchResults = ProviderProperties.getInstance().
        getProperty("omar.client.thinbrowser.numSearchResults");
        if (log.isWarnEnabled()) {
            if (numberOfSearchResults != null  && !numberOfSearchResults.equals("") && 
                    Integer.parseInt(numberOfSearchResults) < MIN_NUMBER_OF_SEARCH_RESULTS) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.DisplayResultsRangeShouldBe1To20"));
                numberOfSearchResults = Integer.toString(MIN_NUMBER_OF_SEARCH_RESULTS);
            }
            else if (numberOfSearchResults != null && !numberOfSearchResults.equals("") &&
                    Integer.parseInt(numberOfSearchResults) > MAX_NUMBER_OF_SEARCH_RESULTS) {
                log.warn(WebUIResourceBundle.getInstance().getString("message.DisplayResultsRangeShouldBe1To20"));
                numberOfSearchResults = Integer.toString(MAX_NUMBER_OF_SEARCH_RESULTS);
            }
            else if(numberOfSearchResults == null || numberOfSearchResults.equals("")) {
                numberOfSearchResults = Integer.toString(DEFAULT_NUMBER_OF_SEARCH_RESULTS);
            }
        }
      return Integer.parseInt(numberOfSearchResults);
    }

    public int getTotalResultCount() {
        return totalResultCount;
    }
    
    public void setTotalResultCount(int totalResultCount) {
        this.totalResultCount = totalResultCount;
    }
    
}
