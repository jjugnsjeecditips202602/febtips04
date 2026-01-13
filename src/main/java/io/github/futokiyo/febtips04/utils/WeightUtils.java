package io.github.futokiyo.febtips04.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class WeightUtils {
	
	static String WEIGHT_SEED;
	
	static {
		try {
			URL fileUrl = WeightUtils.class.getResource("/SJIS_by_UTF8.csv");
			StringBuilder sb = new StringBuilder();
			try (InputStream inputStream = fileUrl.openStream();
                 InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr);) {
				char[] charArrayData = new char[1024];
				int readCharNum = br.read(charArrayData);
				while (readCharNum != -1) {
					sb.append(charArrayData, 0, readCharNum);
					readCharNum = br.read(charArrayData);
				}
			}
			WEIGHT_SEED = sb.toString();
			System.out.println("WEIGHT_SEED.length()" + WEIGHT_SEED.length());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	// prohibit to use constructor
	private WeightUtils() {
	}
	
	public static String getWeigthSeed() {
		return WEIGHT_SEED;
	}
	
	public static String generateWeight(int time) {
		StringBuilder sb = new StringBuilder(UUID.randomUUID().toString());
		for(int i=0;i<time;i++) {
			sb.append(WEIGHT_SEED)
			.append(UUID.randomUUID().toString());
		}
		return sb.toString();
	}

}
