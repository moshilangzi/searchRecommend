/**
 * ��ʼ�����Ա�
 */
package com.yufei.searchrecommend.utils;

import java.util.ResourceBundle;

/**
 * @author Administrator
 *
 */
public class ReadProperties {
	public static ResourceBundle appBundle;
	static {
		appBundle = ResourceBundle.getBundle("yufei-searchRecommend");
	}
}
