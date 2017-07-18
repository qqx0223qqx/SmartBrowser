package com.huarui.life.entity;

/**
 * Created by HR_Life on 2016/11/10 14:42 15:19.
 */

public class VersionEntity {
	/**
	 * status : 0
	 * data : {"name":"appp","version":"1.1.5","filename":"app/Browsers_1.1.5.apk"}
	 */

	private String status;
	private DataBean data;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public DataBean getData() {
		return data;
	}

	public void setData(DataBean data) {
		this.data = data;
	}

	public static class DataBean {
		/**
		 * name : appp
		 * version : 1.1.5
		 * filename : app/Browsers_1.1.5.apk
		 */

		private String name;
		private String version;
		private String filename;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		@Override
		public String toString() {
			return "DataBean{" +
					"name='" + name + '\'' +
					", version='" + version + '\'' +
					", filename='" + filename + '\'' +
					'}';
		}
	}

}
