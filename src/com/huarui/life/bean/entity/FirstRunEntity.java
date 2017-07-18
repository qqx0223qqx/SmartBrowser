package com.huarui.life.bean.entity;

/**
 * Created by HR_Life on 2017/1/11 11:18.
 */

public class FirstRunEntity {
	/**
	 * status : 0
	 * data : {"deviceid":70}
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
		 * deviceid : 70
		 */
		private int deviceid;

		public int getDeviceid() {
			return deviceid;
		}

		public void setDeviceid(int deviceid) {
			this.deviceid = deviceid;
		}
	}
}
