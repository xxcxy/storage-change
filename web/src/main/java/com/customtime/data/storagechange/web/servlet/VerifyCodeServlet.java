package com.customtime.data.storagechange.web.servlet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.customtime.data.storagechange.service.util.StringUtil;
import com.customtime.data.storagechange.web.util.Constants;

public class VerifyCodeServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',   
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',   
            'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' }; 
	private int width = 90;
	private int height = 30;
	private int codeCount = 4;
	protected void service(HttpServletRequest req, HttpServletResponse resp)   
	            throws ServletException, java.io.IOException {   
	        String wt = req.getParameter("width"); 
	        if(StringUtil.isNotBlank(wt))
	        	width = Integer.parseInt(wt);
	        String ht = req.getParameter("height"); 
	        if(StringUtil.isNotBlank(ht))
	        	height = Integer.parseInt(ht);
	        String cc = req.getParameter("codeCount");
	        if(StringUtil.isNotBlank(cc))
	        	codeCount = Integer.parseInt(cc);
	        if(codeCount<4)
	        	codeCount = 4;
	        BufferedImage buffImg = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);   
	        Graphics2D gd = buffImg.createGraphics();   
	        Random random = new Random();   
	        gd.setColor(Color.WHITE);   
	        gd.fillRect(0, 0, width, height);   
	        Font font = new Font("Fixedsys", Font.PLAIN, height-2);   
	        gd.setFont(font);   
	        gd.setColor(Color.BLACK);   
	        gd.drawRect(0, 0, width - 1, height - 1);   
	        gd.setColor(Color.BLACK);   
	        for (int i = 0; i < 160; i++) {   
	            int x = random.nextInt(width);   
	            int y = random.nextInt(height);   
	            int xl = random.nextInt(12);   
	            int yl = random.nextInt(12);   
	            gd.drawLine(x, y, x + xl, y + yl);   
	        }   
	        StringBuilder randomCode = new StringBuilder();   
	        int red = 0, green = 0, blue = 0;   
	        for (int i = 0; i < codeCount; i++) {   
	            String strRand = String.valueOf(codeSequence[random.nextInt(36)]);   
	            red = random.nextInt(255);   
	            green = random.nextInt(255);   
	            blue = random.nextInt(255);   
	            gd.setColor(new Color(red, green, blue));   
	            gd.drawString(strRand, (i + 1) * (width/(codeCount + 1)), height-4);   
	            randomCode.append(strRand);   
	        }   
	        HttpSession session = req.getSession();   
	        session.setAttribute(Constants.VERIFYCODE, randomCode.toString());   
	        resp.setHeader("Pragma", "no-cache");   
	        resp.setHeader("Cache-Control", "no-cache");   
	        resp.setDateHeader("Expires", 0);   
	        resp.setContentType("image/jpeg");   
	        ServletOutputStream sos = resp.getOutputStream();   
	        ImageIO.write(buffImg, "jpeg", sos);   
	        sos.close();   
	    }   
}
