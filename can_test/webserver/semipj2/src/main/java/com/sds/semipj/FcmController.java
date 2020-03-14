package com.sds.semipj;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FcmController {
	

	@RequestMapping(value="/fcm")
    public void fcmTest(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		System.out.println("hi fcm");
		
		
		String ip = request.getParameter("ip");
		String speed = request.getParameter("speed");

		System.out.println("ip : " + ip);
		System.out.println("speed : " + speed);

		URL url = new URL("https://fcm.googleapis.com/fcm/send");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);

		conn.setRequestProperty("Authorization", "key=");
		conn.setRequestProperty("Content-Type", "application/json");

		JSONObject json = new JSONObject();
		json.put("to","key");

		JSONObject info = new JSONObject();
		info.put("title", ip);
		info.put("body", speed);

		json.put("notification", info);

		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		out.write(json.toString());
		out.flush();
		conn.getInputStream();
		
	
	}
}
