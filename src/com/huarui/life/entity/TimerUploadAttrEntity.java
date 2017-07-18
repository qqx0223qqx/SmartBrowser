package com.huarui.life.entity;

/**
 * Created by HR_Life on 2016/12/8 14:42 15:19.
 */

public class TimerUploadAttrEntity {

	/**
	 * status : 0
	 * data : {"cmd":0,"param":""}
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
		 * cmd : 0
		 * param :
		 */

		private String cmd;
		private String param;

		public String getCmd() {
			return cmd;
		}

		public void setCmd(String cmd) {
			this.cmd = cmd;
		}

		public String getParam() {
			return param;
		}

		public void setParam(String param) {
			this.param = param;
		}

		@Override
		public String toString() {
			return "DataBean{" +
					"cmd=" + cmd +
					", param='" + param + '\'' +
					'}';
		}
	}

	@Override
	public String toString() {
		return "TimerUploadAttrEntity{" +
				"status='" + status + '\'' +
				", data=" + data +
				'}';
	}
}
