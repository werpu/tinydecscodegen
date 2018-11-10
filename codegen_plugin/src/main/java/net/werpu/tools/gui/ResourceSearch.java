package net.werpu.tools.gui;

import com.intellij.find.SearchTextArea;
import net.werpu.tools.gui.support.AngularResourceFileTableCellRenderer;
import net.werpu.tools.gui.support.AngularResourceModuleTableCellRenderer;
import net.werpu.tools.gui.support.AngularResourceNameTableCellRenderer;
import lombok.Getter;
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
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setCellRenderer(col1Renderer);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setCellRenderer(col2Renderer);
        columnModel.getColumn(2).setPreferredWidth(400);
    }

    public void createUIComponents() {

        JTextArea txtSearch2 = new JTextArea();
        txtSearch2.setColumns(25);
        txtSearch2.setRows(1);
        txtSearch = new SearchTextArea((JTextArea) txtSearch2, true, false);
        txtSearch.setMultilineEnabled(false);
    }

}
