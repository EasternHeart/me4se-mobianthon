package org.me4se.psi.java1.gcf.socket;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import javax.microedition.io.SocketConnection;
import javax.microedition.io.StreamConnection;

import org.me4se.impl.ConnectionImpl;

public class SocketConnectionImpl extends ConnectionImpl implements StreamConnection, SocketConnection {

	public void initialise(Properties properties) {
		socketProxyHost = properties.getProperty("socketProxyHost");
		if (socketProxyHost != null)
			socketProxyPort = Integer.parseInt(properties.getProperty("socketProxyPort"));
	}

	public static String socketProxyHost = null;
	public static int socketProxyPort = -1;

	public Socket socket;

	public void open(String url, int mode, boolean timeouts) throws IOException {
		// socket://

        
        // System.out.println("Connecting to: "+url);

        
		int cut = url.lastIndexOf(':');

		// added by andre

		//	socket = new Socket
		//	    (url.substring (9, cut),
		//	     Integer.parseInt (url.substring (cut+1)));
		String host;
		int port;

		if (cut >= 9) {
			host = url.substring(9, cut);
			port = Integer.parseInt(url.substring(cut + 1));
		} else {
			host = url.substring(9);
			port = 80;
		}

		if (socketProxyHost == null) {
			socket = new Socket(host, port);
		} else {
			socket = new Socket(socketProxyHost, socketProxyPort);
			BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeUTF(host);
			dos.writeInt(port);
			dos.flush();
		}
		// end added by andre

	}

	public InputStream openInputStream() throws IOException {
		return socket.getInputStream();
	}

	public DataInputStream openDataInputStream() throws IOException {
		return new DataInputStream(openInputStream());
	}

	public OutputStream openOutputStream() throws IOException {
		return socket.getOutputStream();
	}

	public DataOutputStream openDataOutputStream() throws IOException {
		return new DataOutputStream(openOutputStream());
	}

	public void close() throws IOException {
		socket.close();
	}

    public void setSocketOption(byte option, int value) throws IllegalArgumentException, IOException {
        switch(option){
        case SocketConnection.DELAY:
            socket.setTcpNoDelay(option == 0);
            break;
        case SocketConnection.KEEPALIVE:
            socket.setKeepAlive(option != 0);
            break;
        case SocketConnection.LINGER:
            socket.setSoLinger(value != 0, value);
            break;
        case SocketConnection.RCVBUF:
            socket.setReceiveBufferSize(value);
            break;
        case SocketConnection.SNDBUF:
            socket.setSendBufferSize(value);
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    public int getSocketOption(byte option) throws IllegalArgumentException, IOException {
        switch(option){
        case SocketConnection.DELAY:
            return socket.getTcpNoDelay() ? 0 : 1;
        case SocketConnection.KEEPALIVE:
            return socket.getKeepAlive() ? 1 : 0;  
        case SocketConnection.LINGER:
            return socket.getSoLinger();
        case SocketConnection.RCVBUF:
            return socket.getReceiveBufferSize();
        case SocketConnection.SNDBUF:
            return socket.getSendBufferSize();
        default:
            throw new IllegalArgumentException();
        }
    }

    public String getLocalAddress() throws IOException {
        return socket.getLocalAddress().toString();
    }

    public int getLocalPort() throws IOException {
        return socket.getLocalPort();
    }

    public String getAddress() throws IOException {
        return socket.getRemoteSocketAddress().toString();
    }

    public int getPort() throws IOException {
        return socket.getPort();
    }

}