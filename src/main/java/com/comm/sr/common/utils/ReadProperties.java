/**
 * ��ʼ�����Ա�
 */
package com.comm.sr.common.utils;

import java.util.ResourceBundle;

/**
 * @author Administrator
 */
public class ReadProperties {
  public static ResourceBundle appBundle;
  static {
    appBundle = ResourceBundle.getBundle("baihe-searchRecommend");
  }
}
