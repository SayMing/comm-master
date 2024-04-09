package com.yang.serialport.ui;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.yang.serialport.utils.ByteUtils;
import com.yang.serialport.utils.ShowUtils;

public class CarLicensePlatePanel extends JPanel {
    private MainFrame mainFrame;
    
    private JTextField carSearchIdTextField;
    private JTable carTable;
    private JTextField carOldIdTextField;
    private JTextField carNewIdTextField;
    private JTextField lightSearchIdTextField;
    private JTable lightTable;
    private JTextField lightOldIdTextField;
    private JTextField lightNewIdTextField;
    private JTextField lightIdStatusTextField;
    public static final String[] LIGHT_STATUS = new String[] {"熄灭", "绿色", "绿闪", "红色", "红闪"};
    public static final String[] LIGHT_STATUS_CODE = new String[] {"00", "01", "02", "03", "04"};
    private JTextField positionSearchIdTextField;
    private JTable positionTable;
    private JTextField positionOldIdTextField;
    private JTextField positionNewIdTextField;
    private JTextField positionBSearchIdTextField;
    private JTextField positionBOldIdTextField;
    private JTextField positionBNewIdTextField;
    private JTable positionBTable;
    private JComboBox lightStatusBox1, lightStatusBox2, lightStatusBox3, lightStatusBox4;
    private static final Vector<String> TABLE_FIELDS = new Vector(Arrays.asList("序号", "ID", "时间" ));

    /**
     * Create the panel.
     */
    public CarLicensePlatePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        DataDefaultTableModel carDefaultTableModel = new DataDefaultTableModel(new Vector<>(), TABLE_FIELDS);
        DataDefaultTableModel lightDefaultTableModel = new DataDefaultTableModel(new Vector<>(), TABLE_FIELDS);
        DataDefaultTableModel positionDefaultTableModel = new DataDefaultTableModel(new Vector<>(), TABLE_FIELDS);
        DataDefaultTableModel positionBDefaultTableModel = new DataDefaultTableModel(new Vector<>(), TABLE_FIELDS);
        
        setLayout(null);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(0, 0, 680, 450);
        add(tabbedPane);
        
        JPanel carPanel = new JPanel();
        tabbedPane.addTab("电子车牌", null, carPanel, null);
        carPanel.setLayout(null);
        
        JLabel lblNewLabel = new JLabel("搜索ID：");
        lblNewLabel.setBounds(10, 9, 61, 15);
        carPanel.add(lblNewLabel);
        
        carSearchIdTextField = new JTextField();
        carSearchIdTextField.setBounds(71, 6, 120, 21);
        carPanel.add(carSearchIdTextField);
        carSearchIdTextField.setColumns(10);
        
        JButton carSelectOneButton = new JButton("指定搜索");
        carSelectOneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCarSearchOne();
            }
        });
        carSelectOneButton.setBounds(200, 5, 120, 23);
        carPanel.add(carSelectOneButton);
        
        JButton carSelectAllButton = new JButton("广播搜索");
        carSelectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doCarSearchAll();
            }
        });
        carSelectAllButton.setBounds(331, 5, 120, 23);
        carPanel.add(carSelectAllButton);
        
        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(new Rectangle(5, 33, 660, 250));
        carPanel.add(scrollPane_1);
        
        carTable = new JTable();
        carTable.setFont(new java.awt.Font("宋体", java.awt.Font.PLAIN, 18));
        carTable.setRowSelectionAllowed(false);
        carTable.setEnabled(false);
        scrollPane_1.setViewportView(carTable);
        carTable.setModel(carDefaultTableModel);
        carTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        carTable.getColumnModel().getColumn(0).setMinWidth(80);
        carTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        carTable.getColumnModel().getColumn(1).setMinWidth(120);
        carTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        carTable.getColumnModel().getColumn(2).setMinWidth(150);
        carTable.setBorder(null);
        
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u8BBE\u7F6EID", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panel.setBounds(5, 293, 186, 112);
        carPanel.add(panel);
        panel.setLayout(null);
        
        carOldIdTextField = new JTextField();
        carOldIdTextField.setBounds(54, 21, 120, 21);
        panel.add(carOldIdTextField);
        carOldIdTextField.setColumns(10);
        
        carNewIdTextField = new JTextField();
        carNewIdTextField.setBounds(54, 48, 120, 21);
        panel.add(carNewIdTextField);
        carNewIdTextField.setColumns(10);
        
        JButton carIdButton = new JButton("设置");
        carIdButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doChangeCarId();
            }
        });
        carIdButton.setBounds(54, 79, 120, 23);
        panel.add(carIdButton);
        
        JLabel lblid_1 = new JLabel("新ID：");
        lblid_1.setBounds(10, 51, 47, 15);
        panel.add(lblid_1);
        
        JLabel lblid = new JLabel("原ID：");
        lblid.setBounds(10, 24, 47, 15);
        panel.add(lblid);
        
        JButton carCleanButton = new JButton("清除搜索");
        carCleanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((DataDefaultTableModel)carTable.getModel()).setDataVector(null, TABLE_FIELDS);
            }
        });
        carCleanButton.setBounds(461, 5, 120, 23);
        carPanel.add(carCleanButton);
        
        JPanel lightPanel = new JPanel();
        tabbedPane.addTab("红绿灯", null, lightPanel, null);
        lightPanel.setLayout(null);
        
        JLabel lblNewLabel_1 = new JLabel("搜索ID：");
        lblNewLabel_1.setBounds(10, 9, 61, 15);
        lightPanel.add(lblNewLabel_1);
        
        lightSearchIdTextField = new JTextField();
        lightSearchIdTextField.setColumns(10);
        lightSearchIdTextField.setBounds(71, 6, 120, 21);
        lightPanel.add(lightSearchIdTextField);
        
        JButton lightSelectOneButton = new JButton("指定搜索");
        lightSelectOneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLightSearchOne();
            }
        });
        lightSelectOneButton.setBounds(200, 5, 120, 23);
        lightPanel.add(lightSelectOneButton);
        
        JButton lightSelectAllButton = new JButton("广播搜索");
        lightSelectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doLightSearchAll();
            }
        });
        lightSelectAllButton.setBounds(331, 5, 120, 23);
        lightPanel.add(lightSelectAllButton);
        
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(5, 33, 660, 250);
        lightPanel.add(scrollPane);
        
        lightTable = new JTable();
        lightTable.setFont(new java.awt.Font("宋体", java.awt.Font.PLAIN, 18));
        scrollPane.setViewportView(lightTable);
        lightTable.setRowSelectionAllowed(false);
        lightTable.setEnabled(false);
        lightTable.setBorder(null);
        lightTable.setModel(lightDefaultTableModel);
        lightTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        lightTable.getColumnModel().getColumn(0).setMinWidth(80);
        lightTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        lightTable.getColumnModel().getColumn(1).setMinWidth(120);
        lightTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        lightTable.getColumnModel().getColumn(2).setMinWidth(150);
        
        JPanel panel_1 = new JPanel();
        panel_1.setLayout(null);
        panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u8BBE\u7F6EID", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panel_1.setBounds(5, 293, 186, 112);
        lightPanel.add(panel_1);
        
        lightOldIdTextField = new JTextField();
        lightOldIdTextField.setColumns(10);
        lightOldIdTextField.setBounds(54, 21, 120, 21);
        panel_1.add(lightOldIdTextField);
        
        lightNewIdTextField = new JTextField();
        lightNewIdTextField.setColumns(10);
        lightNewIdTextField.setBounds(54, 48, 120, 21);
        panel_1.add(lightNewIdTextField);
        
        JButton lightIdButton = new JButton("设置");
        lightIdButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doChangeLightId();
            }
        });
        lightIdButton.setBounds(54, 79, 120, 23);
        panel_1.add(lightIdButton);
        
        JLabel lblid_1_1 = new JLabel("新ID：");
        lblid_1_1.setBounds(10, 51, 47, 15);
        panel_1.add(lblid_1_1);
        
        JLabel lblid_2 = new JLabel("原ID：");
        lblid_2.setBounds(10, 24, 47, 15);
        panel_1.add(lblid_2);
        
        JPanel panel_2 = new JPanel();
        panel_2.setBorder(new TitledBorder(null, "\u72B6\u6001\u8BBE\u7F6E", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_2.setBounds(206, 293, 459, 112);
        lightPanel.add(panel_2);
        panel_2.setLayout(null);
        
        JLabel lblNewLabel_2 = new JLabel("检测ID：");
        lblNewLabel_2.setBounds(10, 24, 54, 15);
        panel_2.add(lblNewLabel_2);
        
        lightIdStatusTextField = new JTextField();
        lightIdStatusTextField.setBounds(60, 21, 120, 21);
        panel_2.add(lightIdStatusTextField);
        lightIdStatusTextField.setColumns(10);
        
        JLabel label = new JLabel("状态1");
        label.setBounds(10, 51, 45, 15);
        panel_2.add(label);
        
         lightStatusBox1 = new JComboBox();
        lightStatusBox1.setModel(new DefaultComboBoxModel(LIGHT_STATUS));
        lightStatusBox1.setBounds(48, 49, 60, 21);
        panel_2.add(lightStatusBox1);
        
        JLabel label_1 = new JLabel("状态2");
        label_1.setBounds(123, 51, 45, 15);
        panel_2.add(label_1);
        
         lightStatusBox2 = new JComboBox();
        lightStatusBox2.setModel(new DefaultComboBoxModel(LIGHT_STATUS));
        lightStatusBox2.setBounds(160, 49, 60, 21);
        panel_2.add(lightStatusBox2);
        
        JLabel label_2 = new JLabel("状态3");
        label_2.setBounds(10, 83, 45, 15);
        panel_2.add(label_2);
        
         lightStatusBox3 = new JComboBox();
        lightStatusBox3.setModel(new DefaultComboBoxModel(LIGHT_STATUS));
        lightStatusBox3.setBounds(48, 81, 60, 21);
        panel_2.add(lightStatusBox3);
        
        JLabel lblNewLabel_3 = new JLabel("状态4");
        lblNewLabel_3.setBounds(123, 83, 45, 15);
        panel_2.add(lblNewLabel_3);
        
         lightStatusBox4 = new JComboBox();
        lightStatusBox4.setModel(new DefaultComboBoxModel(LIGHT_STATUS));
        lightStatusBox4.setBounds(160, 81, 60, 21);
        panel_2.add(lightStatusBox4);
        
        JButton lightStatusButton = new JButton("控制");
        lightStatusButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doChangeLightStatus();
            }
        });
        lightStatusButton.setBounds(231, 79, 93, 23);
        panel_2.add(lightStatusButton);
        
        JButton lightCleanButton = new JButton("清除搜索");
        lightCleanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            ((DataDefaultTableModel)lightTable.getModel()).setDataVector(new Vector<>(0), TABLE_FIELDS);
            }
        });
        lightCleanButton.setBounds(461, 5, 120, 23);
        lightPanel.add(lightCleanButton);
        
        JPanel positionAPanel = new JPanel();
        tabbedPane.addTab("定位器-定位模块", null, positionAPanel, null);
        positionAPanel.setLayout(null);
        
        JLabel lblNewLabel_1_1 = new JLabel("搜索ID：");
        lblNewLabel_1_1.setBounds(10, 9, 61, 15);
        positionAPanel.add(lblNewLabel_1_1);
        
        positionSearchIdTextField = new JTextField();
        positionSearchIdTextField.setColumns(10);
        positionSearchIdTextField.setBounds(71, 6, 120, 21);
        positionAPanel.add(positionSearchIdTextField);
        
        JButton positionSelectOneButton = new JButton("指定搜索");
        positionSelectOneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doPositionSearchOne();
            }
        });
        positionSelectOneButton.setBounds(200, 5, 120, 23);
        positionAPanel.add(positionSelectOneButton);
        
        JButton positionSelectAllButton = new JButton("广播搜索");
        positionSelectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doPositionSearchAll();
            }
        });
        positionSelectAllButton.setBounds(331, 5, 120, 23);
        positionAPanel.add(positionSelectAllButton);
        
        JScrollPane scrollPane_2 = new JScrollPane();
        scrollPane_2.setBounds(5, 33, 660, 250);
        positionAPanel.add(scrollPane_2);
        
        positionTable = new JTable();
        positionTable.setFont(new java.awt.Font("宋体", java.awt.Font.PLAIN, 18));
        scrollPane_2.setViewportView(positionTable);
        positionTable.setRowSelectionAllowed(false);
        positionTable.setEnabled(false);
        positionTable.setBorder(null);
        positionTable.setModel(positionDefaultTableModel);
        positionTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        positionTable.getColumnModel().getColumn(0).setMinWidth(80);
        positionTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        positionTable.getColumnModel().getColumn(1).setMinWidth(120);
        positionTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        positionTable.getColumnModel().getColumn(2).setMinWidth(150);
        
        JPanel panel_1_1 = new JPanel();
        panel_1_1.setLayout(null);
        panel_1_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u8BBE\u7F6EID", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panel_1_1.setBounds(5, 293, 186, 112);
        positionAPanel.add(panel_1_1);
        
        positionOldIdTextField = new JTextField();
        positionOldIdTextField.setColumns(10);
        positionOldIdTextField.setBounds(54, 21, 120, 21);
        panel_1_1.add(positionOldIdTextField);
        
        positionNewIdTextField = new JTextField();
        positionNewIdTextField.setColumns(10);
        positionNewIdTextField.setBounds(54, 48, 120, 21);
        panel_1_1.add(positionNewIdTextField);
        
        JButton positionIdButton = new JButton("设置");
        positionIdButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doChangePositionId();
            }
        });
        positionIdButton.setBounds(54, 79, 120, 23);
        panel_1_1.add(positionIdButton);
        
        JLabel lblid_1_1_1 = new JLabel("新ID：");
        lblid_1_1_1.setBounds(10, 51, 47, 15);
        panel_1_1.add(lblid_1_1_1);
        
        JLabel lblid_2_1 = new JLabel("原ID：");
        lblid_2_1.setBounds(10, 24, 47, 15);
        panel_1_1.add(lblid_2_1);
        
        JButton positionCleanButton = new JButton("清除搜索");
        positionCleanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            ((DataDefaultTableModel)positionTable.getModel()).setDataVector(new Vector<>(0), TABLE_FIELDS);
            }
        });
        positionCleanButton.setBounds(461, 5, 120, 23);
        positionAPanel.add(positionCleanButton);
        
        JPanel positionBPanel = new JPanel();
        positionBPanel.setLayout(null);
        tabbedPane.addTab("定位器-传输模块", null, positionBPanel, null);
        
        JLabel lblNewLabel_1_1_1 = new JLabel("搜索ID：");
        lblNewLabel_1_1_1.setBounds(10, 9, 61, 15);
        positionBPanel.add(lblNewLabel_1_1_1);
        
        positionBSearchIdTextField = new JTextField();
        positionBSearchIdTextField.setColumns(10);
        positionBSearchIdTextField.setBounds(71, 6, 120, 21);
        positionBPanel.add(positionBSearchIdTextField);
        
        JButton positionBSelectOneButton = new JButton("指定搜索");
        positionBSelectOneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doPositionBSearchOne();
            }
        });
        positionBSelectOneButton.setBounds(200, 5, 120, 23);
        positionBPanel.add(positionBSelectOneButton);
        
        JButton positionBSelectAllButton = new JButton("广播搜索");
        positionBSelectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doPositionBSearchAll();
            }
        });
        positionBSelectAllButton.setBounds(331, 5, 120, 23);
        positionBPanel.add(positionBSelectAllButton);
        
        JScrollPane scrollPane_3 = new JScrollPane();
        scrollPane_3.setBounds(5, 33, 660, 250);
        positionBPanel.add(scrollPane_3);
        
        positionBTable = new JTable();
        positionBTable.setFont(new java.awt.Font("宋体", java.awt.Font.PLAIN, 18));
        positionBTable.setModel(positionBDefaultTableModel);
        scrollPane_3.setViewportView(positionBTable);
        positionBTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        positionBTable.getColumnModel().getColumn(0).setMinWidth(80);
        positionBTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        positionBTable.getColumnModel().getColumn(1).setMinWidth(120);
        positionBTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        positionBTable.getColumnModel().getColumn(2).setMinWidth(150);
        
        JPanel panel_1_1_1 = new JPanel();
        panel_1_1_1.setLayout(null);
        panel_1_1_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u8BBE\u7F6EID", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panel_1_1_1.setBounds(5, 293, 186, 112);
        positionBPanel.add(panel_1_1_1);
        
        positionBOldIdTextField = new JTextField();
        positionBOldIdTextField.setColumns(10);
        positionBOldIdTextField.setBounds(54, 21, 120, 21);
        panel_1_1_1.add(positionBOldIdTextField);
        
        positionBNewIdTextField = new JTextField();
        positionBNewIdTextField.setColumns(10);
        positionBNewIdTextField.setBounds(54, 48, 120, 21);
        panel_1_1_1.add(positionBNewIdTextField);
        
        JButton positionBIdButton = new JButton("设置");
        positionBIdButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doChangePositionBId();
            }
        });
        positionBIdButton.setBounds(54, 79, 120, 23);
        panel_1_1_1.add(positionBIdButton);
        
        JLabel lblid_1_1_1_1 = new JLabel("新ID：");
        lblid_1_1_1_1.setBounds(10, 51, 47, 15);
        panel_1_1_1.add(lblid_1_1_1_1);
        
        JLabel lblid_2_1_1 = new JLabel("原ID：");
        lblid_2_1_1.setBounds(10, 24, 47, 15);
        panel_1_1_1.add(lblid_2_1_1);
        
        JButton positionBCleanButton = new JButton("清除搜索");
        positionBCleanButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((DataDefaultTableModel)positionBTable.getModel()).setDataVector(null, TABLE_FIELDS);
            }
        });
        positionBCleanButton.setBounds(461, 5, 120, 23);
        positionBPanel.add(positionBCleanButton);

        DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();// 设置table内容居中
        tcr.setHorizontalAlignment(SwingConstants.CENTER);// 这句和上句作用一样
        carTable.setDefaultRenderer(Object.class, tcr);

        DefaultTableCellRenderer tcr2 = new DefaultTableCellRenderer();// 设置table内容居中
        tcr2.setHorizontalAlignment(SwingConstants.CENTER);// 这句和上句作用一样
        lightTable.setDefaultRenderer(Object.class, tcr2);

        DefaultTableCellRenderer tcr3 = new DefaultTableCellRenderer();// 设置table内容居中
        tcr3.setHorizontalAlignment(SwingConstants.CENTER);// 这句和上句作用一样
        positionTable.setDefaultRenderer(Object.class, tcr3);

        DefaultTableCellRenderer tcr4 = new DefaultTableCellRenderer();// 设置table内容居中
        tcr4.setHorizontalAlignment(SwingConstants.CENTER);// 这句和上句作用一样
        positionBTable.setDefaultRenderer(Object.class, tcr4);
    }
    
    private static final String NUMBER_REGEX = "^[0-9]*[1-9][0-9]*$";
    
    // 电子车牌Start
    /**
     * 搜索指定电子车牌
     */
    private void doCarSearchOne() {
        String searchId = carSearchIdTextField.getText();
        if(searchId == null) {
            ShowUtils.errorMessage("电子车牌ID不能为空");
            return;
        }
        if(!searchId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("电子车牌ID只能是正整数");
            return;
        }
        // 发送内容 5A 38 02 04 ID SUM
        String[] str = {"5A", "38", "02", "04", ByteUtils.int2Hex(Integer.parseInt(searchId)), ""};
        str[str.length - 1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
        cleanCarTableData();
    }
    
    /**
     * 广播搜索
     */
    private void doCarSearchAll() {
        // 发送内容 5A 38 02 04 FF 97
        String[] str = {"5A", "38", "02", "04", "FF", ""};
        str[str.length - 1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
        cleanCarTableData();
    }
    /**
     * 清除电子车牌列表
     */
    public void cleanCarTableData() {
        carVector.clear();
        ((DataDefaultTableModel)carTable.getModel()).setDataVector(carVector, TABLE_FIELDS);
    }
    public void addCarTableData(String carId) {
        //查重更新
        for(int i=0;i<carVector.size();i++) {
            Vector<String> item = carVector.get(i);
            if(carId.equals(item.get(1))) {
                item.set(2, df.format(LocalDateTime.now()));
                carVector.set(i, item);
                return;
            }
        }
        Vector<String> item = new Vector<String>();
        item.add(Integer.toString(carVector.size() +1));
        item.add(carId);
        item.add(df.format(LocalDateTime.now()));
        carVector.add(item);
    }
    /**
     * 设置电子车牌ID
     */
    private void doChangeCarId() {
        String oldId = carOldIdTextField.getText();
        String newId = carNewIdTextField.getText();
        if(oldId == null) {
            ShowUtils.errorMessage("电子车牌原ID不能为空");
            return;
        }
        if(newId == null) {
            ShowUtils.errorMessage("电子车牌新ID不能为空");
            return;
        }
        if(!oldId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("电子车牌原ID只能是正整数");
            return;
        }
        if(!newId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("电子车牌新ID只能是正整数");
            return;
        }
        // 发送内容 5A 37 03 04 IDX IDY SUM
        String[] str = {"5A", "37", "03", "04", ByteUtils.int2Hex(Integer.parseInt(oldId)), ByteUtils.int2Hex(Integer.parseInt(newId)), ""};
        str[str.length-1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
    }
    // 电子车牌End
    
    // 红绿灯Start
    /**
     * 搜索指定信号灯
     */
    private void doLightSearchOne() {
        String searchId = lightSearchIdTextField.getText();
        if(searchId == null) {
            ShowUtils.errorMessage("信号灯ID不能为空");
            return;
        }
        if(!searchId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("信号灯ID只能是正整数");
            return;
        }
        // 发送内容 5A 38 02 03 ID SUM
        String[] str = {"5A", "38", "02", "03", ByteUtils.int2Hex(Integer.parseInt(searchId)), ""};
        str[str.length - 1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
        cleanLightTableData();
    }
    /**
     * 广播搜索信号灯
     */
    private void doLightSearchAll() {
        // 发送内容 5A 38 02 03 ID SUM
        String[] str = {"5A", "38", "02", "03", "FF", ""};
        str[str.length - 1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
        cleanLightTableData();
    }
    public void cleanLightTableData() {
        lightVector.clear();
        ((DataDefaultTableModel)lightTable.getModel()).setDataVector(lightVector, TABLE_FIELDS);
    }
    public void addLightTableData(String lightId) {
        //查重更新
        for(int i=0;i<lightVector.size();i++) {
            Vector<String> item = lightVector.get(i);
            if(lightId.equals(item.get(1))) {
                item.set(2, df.format(LocalDateTime.now()));
                lightVector.set(i, item);
                return;
            }
        }
        Vector<String> item = new Vector<String>();
        item.add(Integer.toString(lightVector.size() + 1));
        item.add(lightId);
        item.add(df.format(LocalDateTime.now()));
        lightVector.add(item);
    }
    /**
     * 更改信号灯id
     */
    private void doChangeLightId() {
        String oldId = lightOldIdTextField.getText();
        String newId = lightNewIdTextField.getText();
        if(oldId == null) {
            ShowUtils.errorMessage("信号灯原ID不能为空");
            return;
        }
        if(newId == null) {
            ShowUtils.errorMessage("信号灯新ID不能为空");
            return;
        }
        if(!oldId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("信号灯原ID只能是正整数");
            return;
        }
        if(!newId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("信号灯新ID只能是正整数");
            return;
        }
        // 发送内容 5A 37 03 03 IDX IDY SUM
        String[] str = {"5A", "37", "03", "03", ByteUtils.int2Hex(Integer.parseInt(oldId)), ByteUtils.int2Hex(Integer.parseInt(newId)), ""};
        str[str.length-1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
    }
    
    /**
     * 更改信号灯状态
     */
    private void doChangeLightStatus() {
        String id = lightIdStatusTextField.getText();
        if(id == null) {
            ShowUtils.errorMessage("信号灯ID不能为空");
            return;
        }
        if(!id.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("信号灯ID只能是正整数");
            return;
        }
        
        // 5A 39 ID X1 X2 X3 X4
        String[] str = {"5A", "39", ByteUtils.int2Hex(Integer.parseInt(id)),
                LIGHT_STATUS_CODE[lightStatusBox1.getSelectedIndex()], LIGHT_STATUS_CODE[lightStatusBox2.getSelectedIndex()], 
                LIGHT_STATUS_CODE[lightStatusBox3.getSelectedIndex()], LIGHT_STATUS_CODE[lightStatusBox4.getSelectedIndex()], ""};
        str[str.length-1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
    }
    // 红绿灯End
    
    // 定位器Start
    /**
     * 搜索指定定位器
     */
    private void doPositionSearchOne() {
        String searchId = positionSearchIdTextField.getText();
        if(searchId == null) {
            ShowUtils.errorMessage("定位器ID不能为空");
            return;
        }
        if(!searchId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("定位器ID只能是正整数");
            return;
        }
        // 发送内容 5A 38 02 01 ID SUM
        String[] str = {"5A", "38", "02", "01", ByteUtils.int2Hex(Integer.parseInt(searchId)), ""};
        str[str.length - 1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
        cleanPositionTableData();
    }
    /**
     * 广播搜索定位器
     */
    private void doPositionSearchAll() {
        // 发送内容 5A 38 02 01 ID SUM
        String[] str = {"5A", "38", "02", "01", "FF", ""};
        str[str.length - 1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
        cleanPositionTableData();
    }
    public void cleanPositionTableData() {
        positionVector.clear();
        ((DataDefaultTableModel)positionTable.getModel()).setDataVector(positionVector, TABLE_FIELDS);
    }
    public void addPositionTableData(String positionId) {
        //查重更新
        for(int i=0;i<lightVector.size();i++) {
            Vector<String> item = positionVector.get(i);
            if(positionId.equals(item.get(1))) {
                item.set(2, df.format(LocalDateTime.now()));
                return;
            }
        }
        Vector<String> item = new Vector<String>();
        item.add(Integer.toString(positionVector.size() + 1));
        item.add(positionId);
        item.add(df.format(LocalDateTime.now()));
        positionVector.add(item);
    }
    /**
     * 更改定位器id
     */
    private void doChangePositionId() {
        String oldId = positionOldIdTextField.getText();
        String newId = positionNewIdTextField.getText();
        if(oldId == null) {
            ShowUtils.errorMessage("定位器原ID不能为空");
            return;
        }
        if(newId == null) {
            ShowUtils.errorMessage("定位器新ID不能为空");
            return;
        }
        if(!oldId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("定位器原ID只能是正整数");
            return;
        }
        if(!newId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("定位器新ID只能是正整数");
            return;
        }
        // 发送内容 5A 37 03 01 IDX IDY SUM
        String[] str = {"5A", "37", "03", "01", ByteUtils.int2Hex(Integer.parseInt(oldId)), ByteUtils.int2Hex(Integer.parseInt(newId)), ""};
        str[str.length-1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
    }
    
    /**
     * 搜索指定定位器传输模块
     */
    private void doPositionBSearchOne() {
        String searchId = positionBSearchIdTextField.getText();
        if(searchId == null) {
            ShowUtils.errorMessage("定位器ID不能为空");
            return;
        }
        if(!searchId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("定位器ID只能是正整数");
            return;
        }
        // 发送内容 5A 38 02 05 ID SUM
        String[] str = {"5A", "38", "02", "05", ByteUtils.int2Hex(Integer.parseInt(searchId)), ""};
        str[str.length - 1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
        cleanPositionBTableData();
    }
    /**
     * 广播搜索定位器传输模块
     */
    private void doPositionBSearchAll() {
        // 发送内容 5A 38 02 05 ID SUM
        String[] str = {"5A", "38", "02", "05", "FF", ""};
        str[str.length - 1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
        cleanPositionBTableData();
    }
    public void cleanPositionBTableData() {
        positionBVector.clear();
        ((DataDefaultTableModel)positionBTable.getModel()).setDataVector(positionBVector, TABLE_FIELDS);
    }
    public void addPositionBTableData(String id) {
        //查重更新
        for(int i=0;i<lightVector.size();i++) {
            Vector<String> item = positionBVector.get(i);
            if(id.equals(item.get(1))) {
                item.set(2, df.format(LocalDateTime.now()));
                return;
            }
        }
        Vector<String> item = new Vector<String>();
        item.add(Integer.toString(positionBVector.size() + 1));
        item.add(id);
        item.add(df.format(LocalDateTime.now()));
        positionBVector.add(item);
    }
    /**
     * 更改定位器传输模块id
     */
    private void doChangePositionBId() {
        String oldId = positionBOldIdTextField.getText();
        String newId = positionBNewIdTextField.getText();
        if(oldId == null) {
            ShowUtils.errorMessage("定位器原ID不能为空");
            return;
        }
        if(newId == null) {
            ShowUtils.errorMessage("定位器新ID不能为空");
            return;
        }
        if(!oldId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("定位器原ID只能是正整数");
            return;
        }
        if(!newId.matches(NUMBER_REGEX)) {
            ShowUtils.errorMessage("定位器新ID只能是正整数");
            return;
        }
        // 发送内容 5A 37 03 05 IDX IDY SUM
        String[] str = {"5A", "37", "03", "05", ByteUtils.int2Hex(Integer.parseInt(oldId)), ByteUtils.int2Hex(Integer.parseInt(newId)), ""};
        str[str.length-1] = ByteUtils.hex2Low(ByteUtils.hexSum(str));
        mainFrame.sendData(String.join(" ", str));
    }
    // 定位器End

    Vector<Vector> carVector = new Vector<Vector>();
    Vector<Vector> lightVector = new Vector<Vector>();
    Vector<Vector> positionVector = new Vector<Vector>();
    Vector<Vector> positionBVector = new Vector<Vector>();
    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 解析数据
    public void parsing(byte[] data) {
        StringBuffer msg = new StringBuffer();
        boolean refeshData = false;
        // 解析
        for(int i=0; i<data.length;) {
            // 5A A8 02 04 ID SUM 搜索车牌id返回
            // 5A A7 03 01 IDY IDY SUM 设置定位器定位模块返回
            // 5A A7 03 03 IDY IDY SUM 设置信号灯id返回
            // 5A A7 03 04 IDY IDY SUM 设置车牌id返回
            // 5A A7 03 05 IDY IDY SUM 设置定位器传输模块返回
            // 5A 06 ID XX XX SUM 搜索信号灯返回
            // 5A B9 ID X1 X2 X3 X4 设置信号灯状态返回
            // 5A B8 03 01 ID SUM 搜索定位器定位模块返回
            // 5A B8 02 05 ID SUM 搜索定位器传输模块返回
            String x = ByteUtils.byteToHex(data[i]);
            if("5A".equals(x)) {// 5A
                String next = ByteUtils.byteToHex(data[i+1]);
                if("A8".equals(next)) {// A8 搜索车牌id返回
                    String carId = ByteUtils.byteToHex(data[i+4]);
                    addCarTableData(Integer.toString(Integer.parseInt(carId, 16)));
                    i+=6;
                    refeshData = true;
                }else if("A7".equals(next)) {// A7 设置
                    next = ByteUtils.byteToHex(data[i+2]);
                    if("03".equals(next)) {
                        next = ByteUtils.byteToHex(data[i+3]);
                        if("01".equals(next)) {// 定位器定位模块
                            String oldId = Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+4]), 16));
                            String newId = Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+5]), 16));
                            msg.append("设置定位器定位模块ID成功：").append("新ID'").append(newId).append("'。\r\n");
                            i+=7;
                        }else if("03".equals(next)) {
                            String oldId = Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+4]), 16));
                            String newId = Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+5]), 16));
                            msg.append("设置信号灯ID成功：").append("新ID'").append(newId).append("'。\r\n");
                            i+=7;
                        }else if("04".equals(next)) {
                            String oldId = Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+4]), 16));
                            String newId = Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+5]), 16));
                            msg.append("设置车牌ID成功：").append("新ID'").append(newId).append("'。\r\n");
                            i+=7;
                        }else if("05".equals(next)) {
                            String oldId = Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+4]), 16));
                            String newId = Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+5]), 16));
                            msg.append("设置定位器传输模块ID成功：").append("新ID'").append(newId).append("'。\r\n");
                            i+=7;
                        }else {
                            i++;
                        }
                    }else {
                        i++;
                    }
                }else if("06".equals(next)) {// 06
                    // 5A 06 ID XX XX SUM 搜索信号灯返回
                    addLightTableData(Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+2]), 16)));
                    i+=6;
                    refeshData = true;
                }else if("B9".equals(next)) {// B9
                    //5A B9 ID X1 X2 X3 X4 设置信号灯状态返回
                    String id =Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+2]), 16));
                    int status1 = Integer.parseInt(ByteUtils.byteToHex(data[i+3]), 16);
                    int status2 = Integer.parseInt(ByteUtils.byteToHex(data[i+4]), 16);
                    int status3 = Integer.parseInt(ByteUtils.byteToHex(data[i+5]), 16);
                    int status4 = Integer.parseInt(ByteUtils.byteToHex(data[i+6]), 16);
                    msg.append("检测红绿灯ID：'"+id+"'，状态1：").append(formatLightStatus(status1)).append("，状态2：").append(formatLightStatus(status2))
                    .append("，状态3：").append(formatLightStatus(status3)).append("，状态4：").append(formatLightStatus(status4)).append("成功。\r\n");
                    i+=7;
                }else if("B8".equals(next)) {// B8
                    //5A B8 02 05 ID SUM 搜索定位器传输模块返回
                    //5A B8 03 01 ID SUM 搜索定位器定位模块返回
                    next = ByteUtils.byteToHex(data[i+2]);
                    if("02".equals(next)) {
                        String id = Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+4]), 16));
                        addPositionBTableData(id);
                        i+=6;
                        refeshData = true;
                    }else if("03".equals(next)) {
                        String id = Integer.toString(Integer.parseInt(ByteUtils.byteToHex(data[i+4]), 16));
                        addPositionTableData(id);
                        i+=6;
                        refeshData = true;
                    }else {
                        i++;
                    }
                }else {
                    i++;
                }
            }else {
                i++;
            }
        }
        
        if(refeshData) {
            ((DataDefaultTableModel)carTable.getModel()).setDataVector(carVector, TABLE_FIELDS);
            ((DataDefaultTableModel)lightTable.getModel()).setDataVector(lightVector, TABLE_FIELDS);
            ((DataDefaultTableModel)positionTable.getModel()).setDataVector(positionVector, TABLE_FIELDS);
            ((DataDefaultTableModel)positionBTable.getModel()).setDataVector(positionBVector, TABLE_FIELDS);
        }
        if(msg.length() != 0) {
            ShowUtils.message(msg.toString());
        }
    }
    
    private String formatLightStatus(int status) {
        //00熄灭；01绿色；02绿闪；03红色；04红闪
        if(status == 0) {
            return "熄灭";
        }else if(status == 1) {
            return "绿色";
        }else if(status == 2) {
            return "绿闪";
        }else if(status == 3) {
            return "红色";
        }else if(status == 4) {
            return "红闪";
        }else {
            return "未知";
        }
    }
    
    public static class DataDefaultTableModel extends DefaultTableModel{
        public DataDefaultTableModel(String[][] value, String[] titles) {
            super(value, titles);
        }
        public DataDefaultTableModel(Vector value, Vector titles) {
            super(value, titles);
        }
        Class[] columnTypes = new Class[] {
            String.class, String.class, String.class
        };
        public Class getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
        }
    }
}
