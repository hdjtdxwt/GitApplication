package com.epsit.gitapplication.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Administrator on 2018/8/30/030.
 */

public class MacUtil {
    /**
     * 方法一
     * 根据IP地址获取MAC地址
     *
     * @return
     */
    public static String getLocalMacAddressFromIp(Context ctx) {
        String strMacAddr = getRobotId(ctx);
        if (!TextUtils.isEmpty(strMacAddr)) { //获取的mac地址不是空的
            byte[] value = android.util.Base64.encode(strMacAddr.getBytes(), android.util.Base64.DEFAULT); //加密
            strMacAddr = new String(value).trim();
        } else {
        }
        //2018-11-29 发现base64加密后会多一个换行或者多一个空格，所以要trim()去掉才行
        return strMacAddr.trim();
    }

    /**
     * 获取可以标识机器人的唯一编码，首先去获取robotId，没有这么一个东西就获取mac地址
     * @return
     */
    public static String getRobotId(Context ctx){

        //获取机器人的唯一编码
    
            Log.e("_MacUtil", "机器码获取识别，换成mac地址");
            String strMacAddr = "";
            try {
                //获得IpD地址
                InetAddress ip = getLocalInetAddress();
                byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < b.length; i++) {
                    if (i != 0) {
                        buffer.append(':');
                    }
                    String str = Integer.toHexString(b[i] & 0xFF);
                    buffer.append(str.length() == 1 ? 0 + str : str);
                }
                strMacAddr = buffer.toString().toUpperCase();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //2018-11-8 韦武桥在海珠测试发现mac地址获取有时会为空，所以加了以下的两种方法获取
            if (TextUtils.isEmpty(strMacAddr)) {
                strMacAddr = getWifiMac(ctx);
                Log.e("getWifiMac", "第一种方式没获取到，换一种得到的：" + strMacAddr);
                if (TextUtils.isEmpty(strMacAddr)) {
                    strMacAddr = getMac();
                    Log.e("getWifiMac", "第er种方式没获取到，换一种得到的：" + strMacAddr);
                }
            }
            return strMacAddr;
    }
    /**
     * 获取移动设备本地IP
     *
     * @return
     */
    public static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            //列举
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {//是否还有元素
                NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();//得到下一个元素
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();//得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        break;
                    else
                        ip = null;
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {

            e.printStackTrace();
        }
        return ip;
    }

    //方法二
    public static String getWifiMac(Context ctx) {
        if (ctx != null) {
            WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            if (!wifiManager.isWifiEnabled()) {
                //必须先打开，才能获取到MAC地址
                wifiManager.setWifiEnabled(true);
            }
            if (null != info) {
                String str = info.getMacAddress();
                if (str == null) str = "";
                return str;
            } else {
                return "";
            }
        } else {
            return "";
        }

    }

    /**
     * 方法三
     * 这是使用adb shell命令来获取mac地址的方式
     *
     * @return
     */
    public static String getMac() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    /**
     * 获取ip地址的方法
     * @param context
     * @return
     */
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}
