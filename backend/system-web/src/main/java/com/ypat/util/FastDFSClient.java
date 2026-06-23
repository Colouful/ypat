package com.ypat.util;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * FastDFS文件上传下载工具类
 */
@Component
public class FastDFSClient {

    private static Logger logger = Logger.getLogger(FastDFSClient.class);
	private TrackerClient trackerClient = null;
	private TrackerServer trackerServer = null;
	private StorageServer storageServer = null;
	private StorageClient1 storageClient = null;

	static{
		try {
            //String classPath = new File(FastDFSClient.class.getResource("/").getFile()).getCanonicalPath();
			Properties properties = new Properties();
			InputStream inputStream = FastDFSClient.class.getClassLoader().getResourceAsStream("conf/fdfs_client.properties");
			properties.load(inputStream);
			ClientGlobal.initByProperties(properties);
		} catch (Exception e) {
			logger.warn("文件服务器连接异常："+e.getMessage());
		}
	}
	public FastDFSClient() {

	}

	public void createConnection() throws Exception {
		trackerClient = new TrackerClient(ClientGlobal.g_tracker_group);
		trackerServer = trackerClient.getConnection();
		storageServer = null;
		storageClient = new StorageClient1(trackerServer, storageServer);
	}

    public String uploanFile1(InputStream inputStream, String fileName) {
        String[] result = uploadFile(inputStream,fileName,null);
        String res = result[0]+File.separator+result[1];
        return res;
    }

	/**
	 * 上传文件
	 * @param inputStream 文件对象
	 * @param fileName 文件名
	 * @return
	 */
	public  String[] uploadFile(FileInputStream inputStream, String fileName) {
		return uploadFile(inputStream,fileName,null);
	}

	/**
	 * 上传文件
	 * @param inputStream 文件对象
	 * @param fileName 文件名
	 * @param metaList 文件元数据
	 * @return
	 */
	public  String[] uploadFile(InputStream inputStream, String fileName, Map<String,String> metaList) {
		try {
			createConnection();
			byte[] buff = IOUtils.toByteArray(inputStream);
			NameValuePair[] nameValuePairs = null;
			if (metaList != null) {
				nameValuePairs = new NameValuePair[metaList.size()];
				int index = 0;
				for (Iterator<Map.Entry<String,String>> iterator = metaList.entrySet().iterator(); iterator.hasNext();) {
					Map.Entry<String,String> entry = iterator.next();
					String name = entry.getKey();
					String value = entry.getValue();
					nameValuePairs[index++] = new NameValuePair(name,value);
				}
			}
			return storageClient.upload_file(buff,fileName,nameValuePairs);
		} catch (Exception e) {
			logger.error("上传错误："+e);
		}
		return null;
	}

	/**
	 * 获取文件元数据
	 * @param fileId 文件ID
	 * @return
	 */
	public Map<String,String> getFileMetadata(String groupname,String fileId) {
		try {
			NameValuePair[] metaList = storageClient.get_metadata(groupname,fileId);
			if (metaList != null) {
				HashMap<String,String> map = new HashMap<String, String>();
				for (NameValuePair metaItem : metaList) {
					map.put(metaItem.getName(),metaItem.getValue());
				}
				return map;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 删除文件
	 * @param fileId 文件ID
	 * @return 删除失败返回-1，否则返回0
	 */
	public int deleteFile(String groupname,String fileId) {
		try {
			return storageClient.delete_file(groupname,fileId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 下载文件
	 * @param fileId 文件ID（上传文件成功后返回的ID）
	 * @param outFile 文件下载保存位置
	 * @return
	 */
	public  int downloadFile(String groupName,String fileId, File outFile) {
		FileOutputStream fos = null;
		try {
			byte[] content = storageClient.download_file(groupName,fileId);
			fos = new FileOutputStream(outFile);
			InputStream ips = new ByteArrayInputStream(content);
			IOUtils.copy(ips,fos);
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return -1;
	}

	public static void main(String[] args)throws Exception {
		Properties properties = new Properties();
		InputStream inputStream = FastDFSClient.class.getClassLoader().getResourceAsStream("conf/fdfs_client.properties");
		properties.load(inputStream);
		ClientGlobal.initByProperties(properties);
	}

}
