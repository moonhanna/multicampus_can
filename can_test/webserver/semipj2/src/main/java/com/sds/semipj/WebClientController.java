package com.sds.semipj;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sds.semipj.client.Client;

import msg.Msg;

@Controller
public class WebClientController {
	
	Client client = null;

	@RequestMapping("/webclient")
	public void webclient(HttpServletRequest request)
	{
		
		System.out.println("hi servlet");
		
		String ip = request.getParameter("ip");
		String speed = request.getParameter("speed");
		
		System.out.println(ip + " " + speed);
		
		Msg msg = new Msg("Admin", speed, ip);

		try {
			client = new Client("ip", 7777, msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@RequestMapping("/receivepad")
	public void receivepad(HttpServletRequest request)
	{
		
		System.out.println("hi pad");
		
		String id = request.getParameter("id");
		String txt = request.getParameter("txt");
		String tid = request.getParameter("tid");
		
		System.out.println("id : " + id);
		System.out.println("txt " + txt);
		System.out.println("tid " + tid);
		
	}
	
	@RequestMapping("/newClient")
	public void newclient(HttpServletRequest request)
	{
		System.out.println("hi newClient");
		
		String id = request.getParameter("id");
		System.out.println(id);
		

		
	}
	
}
