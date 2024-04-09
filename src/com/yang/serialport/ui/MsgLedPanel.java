package com.yang.serialport.ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import cn.hutool.core.thread.ThreadUtil;

/**
 *
 * @author Sayming-Hong
 * @date 2023-12-05
 */
public class MsgLedPanel extends JPanel {
    private BufferedImage image;
    private ExecutorService executorService = ThreadUtil.newSingleExecutor();
    private Boolean isRun = false;
    /**
     * Create the panel.
     */
    public MsgLedPanel() {
        try {
            image = ImageIO.read(MsgLedPanel.class.getResourceAsStream("/com/yang/serialport/source/msg-led.png"));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void tips() {
        synchronized (isRun) {
            if(isRun == false) {
                executorService.execute(new Runnable(){
                    
                    @Override
                    public void run() {
                        isRun = true;
                        try {
                            int i = 5;
                            while(i > 0) {
                                i--;
                                MsgLedPanel.this.getGraphics().drawImage(image, 0, 0, getWidth(), getHeight(), MsgLedPanel.this);
                                ThreadUtil.sleep(300);
                                MsgLedPanel.this.getGraphics().clearRect(0, 0, getWidth(), getHeight());
                                ThreadUtil.sleep(300);
                            }
                        }finally {
                            isRun = false;
                        }
                        
                    }
                    
                });
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

}
