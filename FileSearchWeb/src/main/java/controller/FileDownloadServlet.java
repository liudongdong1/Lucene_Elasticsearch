package controller;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileDownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String CONTENT_TYPE = "text/html; charset=GBK";
	public FileDownloadServlet() {
		super();
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
		response.reset();
		response.setContentType(CONTENT_TYPE);
		String filename = new String(request.getParameter("filename").getBytes("iso-8859-1"), "UTF-8");
		System.out.println(filename);
		/**
		 * 在servlet里用this.getServletContect().getRealPath()
		 * 在struts里用this.getServlet().getServletContext().getRealPath()
		 * 在Action里用ServletActionContext.getRequest().getRealPath();
		 * 以上三个获得都是当前运行文件在服务器上的绝对路径
		 * */
		System.out.println("文件路径:" + request.getSession().getServletContext().getRealPath("/files") + "/" + filename);
		File file = new File(request.getSession().getServletContext().getRealPath("/files") + "/" + filename);
		System.out.println(file.getPath());
		// 设置response的编码方式
		response.setContentType("application/octet-stream");
		// 写明要下载的文件的大小
		response.setContentLength((int) file.length());
		// 解决中文乱码,向客户端发送返回页面的头信息
		// 1.Content-disposition是MIME协议的扩展
		// 2.attachment --- 作为附件下载
		// 3.在客户端将会弹出下载框
		// 4.这个是文件下载的关键代码
		response.setHeader("Content-Disposition",
				"attachment;filename=" + new String(filename.getBytes("UTF-8"), "ISO8859-1"));
		// 读出文件到i/o流
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream buff = new BufferedInputStream(fis);
		byte[] b = new byte[1024];// 相当于我们的缓存
		int k = 0;// 该值用于计算当前实际下载了多少字节
		// 从response对象中得到输出流,准备下载
		OutputStream myout = response.getOutputStream();
		// 开始循环下载
		while (-1 != (k = fis.read(b, 0, b.length))) {
			// 将b中的数据写到客户端的内存
			myout.write(b, 0, k);
		}
		// 将写入到客户端的内存的数据,刷新到磁盘
		myout.flush();
		fis.close();
		buff.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
