/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

package net.werpu.tools.gui;

import com.intellij.find.SearchTextArea;
import lombok.Getter;
import net.werpu.tools.gui.support.AngularResourceFileTableCellRenderer;
import net.werpu.tools.gui.support.AngularResourceModuleTableCellRenderer;
import net.werpu.tools.gui.support.AngularResourceNameTableCellRenderer;
import net.werpu.tools.supportive.fs.common.IAngularFileContext;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

class RoTableModel extends DefaultTableModel {
    public RoTableModel() {
        super();
    }

    public RoTableModel(int rowCount, int columnCount) {
        super(rowCount, columnCount);
    }

    public RoTableModel(Vector columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public RoTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public RoTableModel(Vector data, Vector columnNames) {
        super(data, columnNames);
    }

    public RoTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    public boolean isCellEditable(int row, int column)      //override isCellEditable
    {
        return false;
    }
}


@Getter
public class ResourceSearch {
    AngularResourceFileTableCellRenderer col2Renderer = new AngularResourceFileTableCellRenderer();
    AngularResourceModuleTableCellRenderer col1Renderer = new AngularResourceModuleTableCellRenderer();
    AngularResourceNameTableCellRenderer col0Renderer = new AngularResourceNameTableCellRenderer();
    private JCheckBox cbControllers;
    private JCheckBox cbComponents;
    private JCheckBox cbServices;
    private JCheckBox cbFilters;
    private JTable tblResults;
    private JPanel panelEditorHolder;
    private JScrollPane panelScroll;
    private JRadioButton rbInProject;
    private JRadioButton rbInModule;
    private JPanel mainPanel;
    private JCheckBox cbModules;
    private SearchTextArea txtSearch;

    public ResourceSearch() {

    }

    public void setupTable() {
        RoTableModel dataModel = new RoTableModel(new Vector(Collections.emptyList()), new Vector(Arrays.asList("Resourcename", "Module", "Location")));

        tblResults.setModel(dataModel);
        TableColumnModel columnModel = tblResults.getColumnModel();

        setupColumns(columnModel);
    }

    public void updateData(List<IAngularFileContext> data) {
        RoTableModel dataModel = new RoTableModel(new Vector(data), new Vector(Arrays.asList("Resourcename", "Module", "Location")));
        tblResults.setModel(dataModel);

        TableColumnModel columnModel = tblResults.getColumnModel();
        setupColumns(columnModel);
        tblResults.setRowSelectionInterval(0, 0);

    }

    private void setupColumns(TableColumnModel columnModel) {
        columnModel.setColumnSelectionAllowed(false);
        columnModel.getColumn(0).setCellRenderer(col0Renderer);
        columnModel.getColumn(0).setPreferredWidth(700);
        columnModel.getColumn(1).setCellRenderer(col1Renderer);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setCellRenderer(col2Renderer);
        columnModel.getColumn(2).setPreferredWidth(400);
    }

    public void createUIComponents() {

        JTextArea txtSearch2 = new JTextArea();
        txtSearch2.setColumns(25);
        txtSearch2.setRows(1);
        txtSearch = new SearchTextArea(txtSearch2, true, false);
        txtSearch.setMultilineEnabled(false);
    }

}
