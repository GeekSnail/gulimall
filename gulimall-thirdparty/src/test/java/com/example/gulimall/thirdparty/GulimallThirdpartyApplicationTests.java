package com.example.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
class GulimallThirdpartyApplicationTests {

	@Test
	void contextLoads() {
	}
	@Autowired
	OSSClient ossClient;
	@Test
	public void ossput() throws FileNotFoundException {
		String bucketName = "202304";
		String key = "favicon.ico";
		FileInputStream inputStream = new FileInputStream("C:\\Users\\35398\\Pictures\\头像\\"+key);
		ossClient.putObject(bucketName, key, inputStream);
		System.out.println("finished.");
	}
}
