package com.sbg.bdd.screenplay.wiremock;

        import java.beans.Introspector;
        import java.beans.PropertyDescriptor;
        import java.net.*;
        import java.util.ArrayList;
        import java.util.Enumeration;
        import java.util.List;


/**
 * Retrieve current host IP address and place it under a configurable project property
 */
public class IpHelper
{


    private String excludedIps;
    private String includedIps;

    public IpHelper() {
        excludedIps="172.,127.";
    }

    public IpHelper(String excludedIps, String includedIps) {
        this.excludedIps = excludedIps;
        this.includedIps = includedIps;
    }

   public String findFirstNonExcludedNetworkInterface() {
        List<InetAddress> addresses = resolveAddresses();
        String[] exclude = getExcludedNetworkInterfaces();
        String[] include = getIncludedNetworkInterfaces();
        return findFirstNonExcluded(addresses, exclude,include);
    }

    private String[] getIncludedNetworkInterfaces() {
        if(includedIps==null || includedIps.length() == 0){
            return null;
        }else{
            return includedIps.split(",");
        }
    }

    private String findFirstNonExcluded(List<InetAddress> addresses, String[] exclude, String[] include) {
        for (InetAddress address : addresses) {
            boolean match = false;
            if(exclude != null && include !=null ){
                match=!matches(exclude,address) && matches(include,address);
            }else if(exclude == null && include == null){
                match=true;
            }else if(include !=null){
                match=matches(include,address);
            }else if(exclude != null){
                match=!matches(exclude,address);
            }
            if(match){
                return address.getHostAddress();
            }
        }
        return null;
    }

    private boolean matches(String[] match, InetAddress address) {
        for (String s : match) {
            if (address.getHostAddress().startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private String[] getExcludedNetworkInterfaces() {
        if(excludedIps==null || excludedIps.length() == 0){
            return null;
        }else{
            return excludedIps.split(",");
        }
    }

    private static List<InetAddress> resolveAddresses() {
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress e = inetAddresses.nextElement();
                    if(e instanceof Inet4Address) {
                        addresses.add(e);
                    }
                }
            }
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
        return addresses;
    }


//    public static void main(String[] args) throws Exception{
//        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//        while (networkInterfaces.hasMoreElements()) {
//            NetworkInterface networkInterface = networkInterfaces.nextElement();
//            System.out.println("###############");
//            System.out.println("networkInterface : " + networkInterface .getDisplayName());
//            for (PropertyDescriptor pd : Introspector.getBeanInfo(NetworkInterface.class).getPropertyDescriptors()) {
//                if(pd.getReadMethod()!=null){
//                    System.out.println(pd.getName() + "=" + pd.getReadMethod().invoke(networkInterface));
//                }
//            }
//        }
//        for (InetAddress address : resolveAddresses()) {
//            System.out.println("###############");
//            System.out.println("Address: " + address.getHostAddress());
//            for (PropertyDescriptor pd : Introspector.getBeanInfo(InetAddress.class).getPropertyDescriptors()) {
//                if(pd.getReadMethod()!=null){
//                    System.out.println(pd.getName() + "=" + pd.getReadMethod().invoke(address));
//                }
//            }
//        }
//    }
}