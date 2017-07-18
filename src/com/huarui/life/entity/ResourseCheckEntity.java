package com.huarui.life.entity;

/**
 * Created by HR_Life on 2017/1/11 17:35.
 */

public class ResourseCheckEntity {
	/**
	 * status : 0
	 * data : {"id":141,"thumb":"http://192.168.1.48/laravel5.1/public/source/php90D8.tmp,http://192.168.1.48/laravel5.1/public/source/phpB8A6.tmp,http://192.168.1.48/laravel5.1/public/source/php446A.tmp,http://192.168.1.48/laravel5.1/public/source/phpB8A6.tmp,http://192.168.1.48/laravel5.1/public/source/phpB8A6.tmp"}
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
		 * id : 141
		 * thumb : http://192.168.1.48/laravel5.1/public/source/php90D8.tmp,http://192.168.1.48/laravel5.1/public/source/phpB8A6.tmp,http://192.168.1.48/laravel5.1/public/source/php446A.tmp,http://192.168.1.48/laravel5.1/public/source/phpB8A6.tmp,http://192.168.1.48/laravel5.1/public/source/phpB8A6.tmp
		 */
		private int id;

		private String thumb;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getThumb() {
			return thumb;
		}

		public void setThumb(String thumb) {
			this.thumb = thumb;
		}
	}
}
