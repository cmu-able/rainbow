package org.sa.rainbow.gui;

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JTable;
import javax.swing.JViewport;

public class JTableCellDisplayer extends ComponentAdapter
    {
        boolean selRow      = false;
        boolean selCol      = false;
        boolean firstTime   = true;
        boolean selectData  = false;
        JTable  table;

        public JTableCellDisplayer(JTable jTable)
        {
            table = jTable;
        }

        @Override
        public void componentResized(ComponentEvent e)
        {
            if (firstTime)
            {
                firstTime = false;
                return;
            }

            int viewIx = table.convertRowIndexToView(table.getRowCount() - 1);

            if (!selRow
                    && !selCol)
            {
                System.out.println(" - Select nothing - selectData="
                        + selectData);
            }
            else if (selRow
                    && !selCol)
            {
                System.out.println(" - Select row only - selectData="
                        + selectData);
            }
            else if (!selRow
                    && selCol)
            {
                System.out.println(" - Select column only - selectData="
                        + selectData);
            }
            else
            {
                System.out.println(" - Select cell - selectData="
                        + selectData);
            }

            // If data should be selected, set the selection policies on the table.
            if (selectData)
            {
                table.setRowSelectionAllowed(selRow);
                table.setColumnSelectionAllowed(selCol);
            }

            // Scroll to the VALUE cell (columnIndex=0) that was added
            displayTableCell(table, viewIx, table.convertColumnIndexToView(0), selectData);

            // Cycle through all possibilities
            if (!selRow
                    && !selCol)
            {
                selRow = true;
            }
            else if (selRow
                    && !selCol)
            {
                selRow = false;
                selCol = true;
            }
            else if (!selRow
                    && selCol)
            {
                selRow = true;
                selCol = true;
            }
            else
            {
                selRow = false;
                selCol = false;
                selectData = !selectData;
            }

        }
        
        protected void displayTableCell(JTable table, int vRowIndex, int vColIndex, boolean selectCell)
        {
            if (!(table.getParent() instanceof JViewport))
            {
                return;
            }

            JViewport viewport = (JViewport) table.getParent();

            /* This rectangle is relative to the table where the
             * northwest corner of cell (0,0) is always (0,0).
             */
            Rectangle rect = table.getCellRect(vRowIndex, vColIndex, true);

            // The location of the view relative to the table
            Rectangle viewRect = viewport.getViewRect();

            /*
             *  Translate the cell location so that it is relative
             *  to the view, assuming the northwest corner of the
             *  view is (0,0).
             */
            rect.setLocation(rect.x
                    - viewRect.x, rect.y
                    - viewRect.y);

            // Calculate location of rectangle if it were at the center of view
            int centerX = (viewRect.width - rect.width) / 2;
            int centerY = (viewRect.height - rect.height) / 2;

            /*
             *  Fake the location of the cell so that scrollRectToVisible
             *  will move the cell to the center
             */
            if (rect.x < centerX)
            {
                centerX = -centerX;
            }
            if (rect.y < centerY)
            {
                centerY = -centerY;
            }
            rect.translate(centerX, centerY);

            // If desired and allowed, select the appropriate cell
            if (selectCell
                    && (table.getRowSelectionAllowed() || table.getColumnSelectionAllowed()))
            {
                // Clear any previous selection
                table.clearSelection();

                table.setRowSelectionInterval(vRowIndex, vRowIndex);
                table.setColumnSelectionInterval(vColIndex, vColIndex);
            }

            // Scroll the area into view.
            viewport.scrollRectToVisible(rect);
        }
    }
