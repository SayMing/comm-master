package com.yang.serialport.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yang.serialport.manager.SerialPortHandler;
import com.yang.serialport.model.Msg;
import com.yang.serialport.model.WorkLineInfo;
import com.yang.serialport.utils.DB;
import com.yang.serialport.utils.WorkTimerUtils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;


/**
 * 程序启动的串口号记录在系统环境变量名为COMM_PORT下。值默认“COM2”
 * @author Sayming-Hong
 * @date 2023-12-05
 */
public class CarMainFrame {
    private static Logger logger = LoggerFactory.getLogger(CarMainFrame.class);
    public static final Date RUN_DATE = new Date();
    private JWindow jWindow;
    private JFrame frame;
    private MsgLedPanel msgLedPanel;
    private JLabel carSumLabel, classesLabel, workTimeLabel, driverNameLabel,
    jobNumberLabel, currentLineStartLabel, currentLineEndLabel, nextLineStartLabel, 
    nextLineEndLabel, statusLabel, nowTime, msgCountLabel;
    private JTextPane msgTextPane;
    private JButton nextButton, upButton;
    private Msg currentMsg;
    private WorkTimerUtils workTimerUtils;
    private static final int LABEL_FONT_SIZE = 36;
    SerialPortHandler serialPortHandler;
    private static final String VERSION = "V1.2";
    private static final String MSG_FORMAT = "【%s】 %s";
    private static final String MSG_COUNT_FORMAT = "第%d条/共%d条";
//    private JTextPane receiveText;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        TimeInterval timer = DateUtil.timer();
        logger.info("main start:{}", timer.interval());
        DB.getInstance().delWorkTimeInfo();

        logger.info("main start run:{}", timer.intervalRestart());
        CarMainFrame window = new CarMainFrame();
        logger.info("main start window over:{}", timer.intervalRestart());
        window.alwaysOnTopMaxSize();
//        if(Constants.isDebug()) {
//            ThreadUtil.schedule(ThreadUtil.createScheduledExecutor(1), ()->window.msgLedPanel.tips(), 0, 5*1000L, false);
//        }

        // 创建并注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
              System.out.println("Shutdown Hook is running");
              // 在这里添加你的清理或其他退出时需要执行的操作
              DB.getInstance().close();
            }
        }));
        logger.info("main start hook:{}", timer.intervalRestart());
        logger.info("main end:{}", timer.intervalRestart());
    }
    
    public void setCarSumLabel(String str) {
        carSumLabel.setText(str);
    }
    public void setClassesLabel(String str) {
        classesLabel.setText(str);
    }
    public void setWorkTimeLabel(String str) {
        workTimeLabel.setText(str);
    }
    public void setDriverNameLabel(String str) {
        driverNameLabel.setText(str);
    }
    public void setJobNumberLabel(String str) {
        jobNumberLabel.setText(str);
    }
    public void setCurrentLineStartLabel(String str) {
        currentLineStartLabel.setText(str);
    }
    public void setCurrentLineEndLabel(String str) {
        currentLineEndLabel.setText(str);
    }
    public void setNextLineStartLabel(String str) {
        nextLineStartLabel.setText(str);
    }
    public void setNextLineEndLabel(String str) {
        nextLineEndLabel.setText(str);
    }
    public void setStatusLabel(String str) {
        statusLabel.setText(str);
    }
    public void setMsgText(String str) {
        msgTextPane.setText(str);
    }
    public void tips() {
        this.msgLedPanel.tips();
    }
    public void nowTime() {
        this.nowTime.setText(DateUtil.now());
    }
    public void setMsgCount(int current, int total) {
        this.msgCountLabel.setText(String.format(MSG_COUNT_FORMAT, current, total));
    }
    public void addReceiveText(String str) {
//        int MAX_LENGTH = 100;
//        StringBuilder sBuilder = new StringBuilder(this.receiveText.getText());
//        if(sBuilder.length() > MAX_LENGTH) {
//            sBuilder.delete(0, str.length());
//        }
//        sBuilder.append(DateUtil.now()).append(" ").append(str).append("\r");  
//        this.receiveText.setText(sBuilder.toString());
    }
    public void cleanNextLineStartLabel() {
        setNextLineStartLabel("");
        setNextLineEndLabel("");
    }
    public void updateNextLineStartLabel() {
        setNextLineStartLabel(currentLineStartLabel.getText());
        setNextLineEndLabel(currentLineEndLabel.getText());
    }
    public void setWorkLineInfo(WorkLineInfo workLineInfo) {
        if(workLineInfo != null) {
            setCurrentLineStartLabel(StrUtil.isNotBlank(workLineInfo.getCurrentBeaconStartLocation()) ? workLineInfo.getCurrentBeaconStartLocation() : "");
            setCurrentLineEndLabel(StrUtil.isNotBlank(workLineInfo.getCurrentBeaconEndLocation()) ? workLineInfo.getCurrentBeaconEndLocation() : "");
            setNextLineStartLabel(StrUtil.isNotBlank(workLineInfo.getNextBeaconStartLocation()) ? workLineInfo.getNextBeaconStartLocation() : "");
            setNextLineEndLabel(StrUtil.isNotBlank(workLineInfo.getNextBeaconEndLocation()) ? workLineInfo.getNextBeaconEndLocation() : "");
        }else {
            setCurrentLineStartLabel("AAAAA");
            setCurrentLineEndLabel("XXXXX");
            setNextLineStartLabel("CCCCC");
            setNextLineEndLabel("DDDDD");
        }
    }
    
    public JFrame getFrame() {
        return frame;
    }

    public void relinkSerialPort() {
        serialPortHandler = null;
        serialPortHandler = new SerialPortHandler(CarMainFrame.this);
        serialPortHandler.openSerialPort();
        serialPortHandler.sendBeaconInfoDataInit();
    }
    
    /**
     * Create the application.
     */
    public CarMainFrame() {
        initialize();
        ThreadUtil.execAsync(new Runnable(){
            @Override
            public void run() {
              currentMsg = DB.getInstance().queryLatestMsg();
              setMsgText(currentMsg);
              workTimerUtils = new WorkTimerUtils(CarMainFrame.this);
              workTimerUtils.startTimer(1);
              WorkLineInfo workLineInfo = DB.getInstance().selectWorkLineInfo();
              setWorkLineInfo(workLineInfo);
              relinkSerialPort();
            }
        });
    }
    
    public void setMsgText(Msg msg) {
        int total = 0;
        int currentNum = 0;
        if(msg == null) {
            msgTextPane.setText("无最新通知");
        }else {
            msgTextPane.setText(String.format(MSG_FORMAT, DateUtil.formatDateTime(new Date(msg.getCreateTime())), msg.getMsg()));
            total = DB.getInstance().countMsg();
            currentNum = DB.getInstance().countNowMsg(msg.getId());
            setMsgCount(currentNum, total);
        }
    }
    
    private void alwaysOnTopMaxSize() {
        // 最大化
        this.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.frame.setAlwaysOnTop(true);
        this.frame.setVisible(true);
        this.jWindow.setAlwaysOnTop(true);
        this.jWindow.setLocation(0, 0);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        msgLedPanel = new MsgLedPanel();
        msgLedPanel.setBounds(36, 5, 38, 38);
        msgLedPanel.setBackground(Color.BLACK);
        msgLedPanel.setPreferredSize(new Dimension(26, 26));
        msgLedPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        msgLedPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        msgLedPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                msgLedPanel.tips();
            }
        });
        frame = new JFrame();
        // 取消标题栏
        frame.setUndecorated(true);
        // 禁止改变窗口大小
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        jWindow = new JWindow(frame);
        frame.addWindowStateListener(new WindowStateListener()
        {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if(e.getNewState() == 0) {
                    CarMainFrame.this.alwaysOnTopMaxSize();
                }
            }
        });
        
        JPanel heardPanel = new JPanel();
        heardPanel.setBorder(new LineBorder(Color.GREEN));
        heardPanel.setBackground(Color.BLACK);
        frame.getContentPane().add(heardPanel, BorderLayout.NORTH);
        heardPanel.setLayout(new BorderLayout(0, 0));
        
        JPanel msgPanel = new JPanel();
        msgPanel.setMinimumSize(new Dimension(28, 38));
        msgPanel.setPreferredSize(new Dimension(255, 32));
        msgPanel.setBackground(Color.BLACK);
        msgPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        msgPanel.setAlignmentX(0.0f);
        heardPanel.add(msgPanel, BorderLayout.WEST);
        
        JLabel lblNewLabel = new JLabel("无轨运矿车辆卸矿调度系统 "+ VERSION);
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel.setForeground(Color.GREEN);
        lblNewLabel.setBackground(Color.BLACK);
        lblNewLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        lblNewLabel.setFont(new Font("宋体", Font.PLAIN, 34));
        heardPanel.add(lblNewLabel, BorderLayout.CENTER);
        frame.setBackground(Color.BLACK);
        frame.setAlwaysOnTop(true);
        frame.setBounds(100, 100, 1024, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        msgPanel.setLayout(null);
        msgLedPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
//        msgPanel.add(msgLedPanel);
        
        JPanel panel_1 = new JPanel();
        panel_1.setPreferredSize(new Dimension(160, 38));
        panel_1.setBackground(Color.BLACK);
        heardPanel.add(panel_1, BorderLayout.EAST);
        
        panel_1.add(msgLedPanel);
        
        JLabel lblNewLabel_11 = new JLabel("最小化");
        lblNewLabel_11.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                frame.setExtendedState(JFrame.ICONIFIED);
            }
        });
        panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        lblNewLabel_11.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_11.setFont(new Font("宋体", Font.PLAIN, 26));
        lblNewLabel_11.setForeground(Color.GREEN);
        panel_1.add(lblNewLabel_11);
        
        JPanel centerPanel = new JPanel();
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
        
        JPanel cp_1 = new JPanel();
        cp_1.setBorder(new MatteBorder(0, 1, 1, 1, (Color) Color.GREEN));
        
        JPanel cp_2 = new JPanel();
        cp_2.setBorder(new MatteBorder(0, 1, 1, 1, (Color) Color.GREEN));
        
        JPanel cp_3 = new JPanel();
        cp_3.setBorder(new MatteBorder(0, 1, 1, 1, (Color) Color.GREEN));
        centerPanel.setLayout(new GridLayout(0, 1, 0, 0));
        centerPanel.add(cp_1);
        cp_1.setLayout(new BorderLayout(0, 0));
        
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setBorder(new MatteBorder(0, 0, 0, 1, (Color) Color.GREEN));
        cp_1.add(panel, BorderLayout.WEST);
        panel.setLayout(new CardLayout(0, 0));
        
        JLabel lblNewLabel_1 = new JLabel("<html>&nbsp;&nbsp;运矿&nbsp;&nbsp;<br>&nbsp;&nbsp;信息&nbsp;&nbsp;</html>");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel_1.setBackground(Color.BLACK);
        lblNewLabel_1.setForeground(Color.GREEN);
        lblNewLabel_1.setHorizontalTextPosition(SwingConstants.CENTER);
        panel.add(lblNewLabel_1, "name_105301940677100");
        lblNewLabel_1.setFont(new Font("宋体", Font.PLAIN, 38));
        
        JPanel panel_4 = new JPanel();
        panel_4.setBackground(Color.BLACK);
        cp_1.add(panel_4, BorderLayout.CENTER);
        panel_4.setLayout(null);
        
        JLabel lblNewLabel_4 = new JLabel("工作时间：");
        lblNewLabel_4.setForeground(Color.GREEN);
        lblNewLabel_4.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        lblNewLabel_4.setBounds(10, 103, 180, 38);
        panel_4.add(lblNewLabel_4);
        
        JLabel lblNewLabel_5 = new JLabel("司机姓名：");
        lblNewLabel_5.setForeground(Color.GREEN);
        lblNewLabel_5.setHorizontalAlignment(SwingConstants.LEFT);
        lblNewLabel_5.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        lblNewLabel_5.setBounds(10, 169, 180, 38);
        panel_4.add(lblNewLabel_5);
        
        JLabel lblNewLabel_6 = new JLabel("卸矿次数：");
        lblNewLabel_6.setForeground(Color.GREEN);
        lblNewLabel_6.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        lblNewLabel_6.setBounds(10, 37, 180, 38);
        panel_4.add(lblNewLabel_6);
        
        carSumLabel = new JLabel("");//999 车
        carSumLabel.setForeground(Color.GREEN);
        carSumLabel.setHorizontalAlignment(SwingConstants.LEFT);
        carSumLabel.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        carSumLabel.setBounds(200, 37, 160, 38);
        panel_4.add(carSumLabel);
        
        JLabel lblNewLabel_9 = new JLabel("当前班次：");
        lblNewLabel_9.setForeground(Color.GREEN);
        lblNewLabel_9.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        lblNewLabel_9.setBounds(449, 37, 180, 38);
        panel_4.add(lblNewLabel_9);
        
        classesLabel = new JLabel("早班(08:01 - 08:01)");//早班（8:00 ~ 10:00）
        classesLabel.setForeground(Color.GREEN);
        classesLabel.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        classesLabel.setBounds(639, 37, 350, 38);
        panel_4.add(classesLabel);
        
        workTimeLabel = new JLabel("");// 10小时10分钟
        workTimeLabel.setForeground(Color.GREEN);
        workTimeLabel.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        workTimeLabel.setBounds(200, 103, 537, 38);
        panel_4.add(workTimeLabel);
        
        driverNameLabel = new JLabel("");
        driverNameLabel.setForeground(Color.GREEN);
        driverNameLabel.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        driverNameLabel.setBounds(200, 169, 239, 38);
        panel_4.add(driverNameLabel);
        
        JLabel lblNewLabel_12 = new JLabel("司机工号：");
        lblNewLabel_12.setForeground(Color.GREEN);
        lblNewLabel_12.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        lblNewLabel_12.setBounds(449, 169, 180, 38);
        panel_4.add(lblNewLabel_12);
        
        jobNumberLabel = new JLabel("");// 12345678
        jobNumberLabel.setForeground(Color.GREEN);
        jobNumberLabel.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        jobNumberLabel.setBounds(639, 169, 220, 38);
        panel_4.add(jobNumberLabel);
        centerPanel.add(cp_2);
        cp_2.setLayout(new BorderLayout(0, 0));
        
        JPanel panel_2 = new JPanel();
        panel_2.setBackground(Color.BLACK);
        panel_2.setBorder(new MatteBorder(0, 0, 0, 1, (Color) Color.GREEN));
        cp_2.add(panel_2, BorderLayout.WEST);
        panel_2.setLayout(new CardLayout(0, 0));
        
        JLabel lblNewLabel_2 = new JLabel("<html>&nbsp;&nbsp;运矿&nbsp;&nbsp;<br>&nbsp;&nbsp;路线&nbsp;&nbsp;</html> ");
        lblNewLabel_2.setBackground(Color.BLACK);
        lblNewLabel_2.setForeground(Color.GREEN);
        panel_2.add(lblNewLabel_2, "name_817705849167300");
        lblNewLabel_2.setFont(new Font("宋体", Font.PLAIN, 38));
        lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel panel_5 = new JPanel();
        panel_5.setBackground(Color.BLACK);
        cp_2.add(panel_5, BorderLayout.CENTER);
        panel_5.setLayout(null);
        
        JLabel lblNewLabel_7 = new JLabel("本次运矿路线：");
        lblNewLabel_7.setForeground(Color.GREEN);
        lblNewLabel_7.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        lblNewLabel_7.setBounds(10, 38, 260, 38);
        panel_5.add(lblNewLabel_7);
        
        JLabel lblNewLabel_8 = new JLabel("状        态：");
        lblNewLabel_8.setForeground(Color.GREEN);
        lblNewLabel_8.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        lblNewLabel_8.setBounds(10, 104, 260, 38);
        panel_5.add(lblNewLabel_8);
        
        JLabel lblNewLabel_10 = new JLabel("下次运矿路线：");
        lblNewLabel_10.setForeground(Color.GREEN);
        lblNewLabel_10.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        lblNewLabel_10.setBounds(10, 170, 260, 38);
        panel_5.add(lblNewLabel_10);
        
        JPanel panel_6 = new JPanel();
        panel_6.setBackground(Color.BLACK);
        panel_6.setBounds(280, 38, 539, 38);
        panel_5.add(panel_6);
        panel_6.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        
        currentLineStartLabel = new JLabel("");//开始点A
        currentLineStartLabel.setHorizontalAlignment(SwingConstants.CENTER);
        currentLineStartLabel.setForeground(Color.GREEN);
        currentLineStartLabel.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        panel_6.add(currentLineStartLabel);
        
        JLabel lblNewLabel_13 = new JLabel(" → ");
        lblNewLabel_13.setForeground(Color.GREEN);
        lblNewLabel_13.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        panel_6.add(lblNewLabel_13);
        
        currentLineEndLabel = new JLabel("");//结束点A
        currentLineEndLabel.setHorizontalAlignment(SwingConstants.CENTER);
        currentLineEndLabel.setForeground(Color.GREEN);
        currentLineEndLabel.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        panel_6.add(currentLineEndLabel);
        
        statusLabel = new JLabel("");//去装矿
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusLabel.setForeground(Color.GREEN);
        statusLabel.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        statusLabel.setBounds(280, 104, 435, 38);
        panel_5.add(statusLabel);
        
        JPanel panel_7 = new JPanel();
        panel_7.setBackground(Color.BLACK);
        FlowLayout flowLayout = (FlowLayout) panel_7.getLayout();
        flowLayout.setAlignment(FlowLayout.LEFT);
        flowLayout.setHgap(0);
        flowLayout.setVgap(0);
        panel_7.setBounds(280, 170, 539, 38);
        panel_5.add(panel_7);
        
        nextLineStartLabel = new JLabel("");//开始点B
        nextLineStartLabel.setForeground(Color.GREEN);
        nextLineStartLabel.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        panel_7.add(nextLineStartLabel);
        
        JLabel lblNewLabel_14 = new JLabel(" → ");
        lblNewLabel_14.setForeground(Color.GREEN);
        lblNewLabel_14.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        panel_7.add(lblNewLabel_14);
        
        nextLineEndLabel = new JLabel("");//结束点B
        nextLineEndLabel.setForeground(Color.GREEN);
        nextLineEndLabel.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        panel_7.add(nextLineEndLabel);
        centerPanel.add(cp_3);
        cp_3.setLayout(new BorderLayout(0, 0));
        
        JPanel panel_3 = new JPanel();
        panel_3.setBackground(Color.BLACK);
        panel_3.setBorder(new MatteBorder(0, 0, 0, 1, (Color) Color.GREEN));
        cp_3.add(panel_3, BorderLayout.WEST);
        panel_3.setLayout(new CardLayout(0, 0));
        
        JLabel lblNewLabel_3 = new JLabel("<html>&nbsp;&nbsp;通知&nbsp;&nbsp;<br>&nbsp;&nbsp;信息&nbsp;&nbsp;</html>");
        lblNewLabel_3.setBackground(Color.BLACK);
        lblNewLabel_3.setForeground(Color.GREEN);
        panel_3.add(lblNewLabel_3, "name_817726086010000");
        lblNewLabel_3.setFont(new Font("宋体", Font.PLAIN, 38));
        lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel panel_8 = new JPanel();
        cp_3.add(panel_8, BorderLayout.CENTER);
        panel_8.setLayout(new BorderLayout(0, 0));
        
        JPanel panel_9 = new JPanel();
        panel_9.setBackground(Color.BLACK);
        panel_8.add(panel_9, BorderLayout.SOUTH);
        
        upButton = new JButton("上一条");
        upButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Msg msg = null;
                if(currentMsg == null) {
                    msg = DB.getInstance().queryLatestMsg();
                }else {
                    msg = DB.getInstance().queryPrevMsg(currentMsg.getId());
                }
                
                if(msg != null) {
                    currentMsg = msg;
                }
                setMsgText(currentMsg);
            }
        });
        upButton.setForeground(Color.DARK_GRAY);
        upButton.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        panel_9.add(upButton);
        
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(20, 5));
        panel_9.add(separator);
        
        nextButton = new JButton("下一条");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Msg msg = null;
                if(currentMsg == null) {
                    msg = DB.getInstance().queryLatestMsg();
                }else {
                    msg = DB.getInstance().queryNextMsg(currentMsg.getId());
                }
                
                if(msg != null) {
                    currentMsg = msg;
                }
                setMsgText(currentMsg);
            }
        });
        nextButton.setForeground(Color.DARK_GRAY);
        nextButton.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        panel_9.add(nextButton);
        
        msgCountLabel = new JLabel("第0条/共0条");
        msgCountLabel.setForeground(Color.GREEN);
        msgCountLabel.setBackground(Color.BLACK);
        msgCountLabel.setFont(new Font("宋体", Font.PLAIN, 38));
        panel_9.add(msgCountLabel);
        
        nowTime = new JLabel("yyyy-MM-dd HH:mm:ss");
        nowTime.setLocation(5, 0);
        nowTime.setSize(255, 37);
        nowTime.setForeground(Color.GREEN);
        nowTime.setFont(new Font("宋体", Font.PLAIN, 26));
        msgPanel.add(nowTime);
        
        msgTextPane = new JTextPane();
        msgTextPane.setMargin(new Insets(10, 10, 10, 10));
        msgTextPane.setForeground(Color.GREEN);
        msgTextPane.setFont(new Font("宋体", Font.PLAIN, LABEL_FONT_SIZE));
        msgTextPane.setEditable(false);
        msgTextPane.setBackground(Color.BLACK);
        panel_8.add(msgTextPane, BorderLayout.CENTER);
        
//        receiveText = new JTextPane();
//        receiveText.setEditable(false);
//        centerPanel.add(new JScrollPane(receiveText));
    }
}
