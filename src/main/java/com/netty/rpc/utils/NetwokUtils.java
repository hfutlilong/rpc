package com.netty.rpc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 获取本机ip
 */
public class NetwokUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetwokUtils.class);

    public static final String LOCALHOST = "127.0.0.1";

    public static final String ANYHOST = "0.0.0.0";

    public static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    /**
     * 获取机器的mac地址
     *
     * @return 返回本机Mac地址
     */
    public static String getLocalMacAddr() {
        try {
            InetAddress ia = InetAddress.getLocalHost();

            // 获取网卡，获取地址
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                // 字节转换为整数
                int temp = mac[i] & 0xff;
                String str = Integer.toHexString(temp);
                if (str.length() == 1) {
                    sb.append("0").append(str);
                } else {
                    sb.append(str);
                }
            }

            return sb.toString().toUpperCase();
        } catch (Exception e) {
            LOGGER.error("getLocalMacAddr fail", e);
        }
        return null;
    }

    public static String getLocalhost() {
        InetAddress address = getLocalAddress();

        if (null == address) {
            LOGGER.error("Could not get local host ip address, will use 127.0.0.1 instead.");
            return LOCALHOST;
        } else {
            return address.getHostAddress();
        }
    }

    public String getLocalhost4Instance() {
        return getLocalhost();
    }

    /**
     * 遍历本地网卡，返回第一个合理的IP
     *
     * @return 本地网卡IP
     */
    public static InetAddress getLocalAddress() {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to retrieving ip address:{}.", e.getMessage(), e);
        }

        return getLocalAddressByNetworkInterfaces();
    }

    private static InetAddress getLocalAddressByNetworkInterfaces() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface network = networkInterfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (network.isLoopback() || !network.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet6Address) {
                        continue;
                    }

                    if (isValidAddress(address)) {
                        return address;
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.warn("Failed to retrieving ip address, " + e.getMessage(), e);
        }

        return null;
    }

    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }

        String name = address.getHostAddress();
        return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN.matcher(name).matches());
    }
}
