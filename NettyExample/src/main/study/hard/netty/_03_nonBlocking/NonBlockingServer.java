package study.hard.netty._03_nonBlocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Run this application. 
 * And connect with telnet
 * 		$ telnet localhost 8888
 * And type anything!
 * 
 * @author SeomGi, Han (iandmyhand@gmail.com)
 *
 */
public class NonBlockingServer {
	private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
	private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);
	
	private void startEchoServer() {
		try(
			Selector selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		) {
			
			if((serverSocketChannel.isOpen()) && selector.isOpen()) {
				serverSocketChannel.configureBlocking(false);
				serverSocketChannel.bind(new InetSocketAddress(8888));
				
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
				System.out.println("standby for connection.");
				
				while(true) {
					selector.select();
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
					
					while(keys.hasNext()) {
						SelectionKey key = (SelectionKey) keys.next();
						keys.remove();
						
						if(!key.isValid()) {
							continue;
						}
						
						if(key.isAcceptable()) {
							this.acceptOP(key, selector);
						} else if (key.isReadable()) {
							this.readOP(key);
						} else if (key.isWritable()) {
							this.writeOP(key);
						}
					}
				}
			} else {
				System.out.println("Didn't create server socket!!!");
			}
			
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	private void acceptOP(SelectionKey key, Selector selector) throws IOException {
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = serverChannel.accept();
		socketChannel.configureBlocking(false);
		
		System.out.println("Connected with Client: " + socketChannel.getRemoteAddress());
		
		keepDataTrack.put(socketChannel, new ArrayList<byte[]>());
		socketChannel.register(selector, SelectionKey.OP_READ);
	}

	private void readOP(SelectionKey key) {
		try {
			SocketChannel socketChannel = (SocketChannel) key.channel();
			buffer.clear();
			int numRead = -1;
			try{
				numRead = socketChannel.read(buffer);
			} catch(IOException e) {
				System.err.println("Failed to read data!!!");
			}
			
			if(numRead == -1) {
				this.keepDataTrack.remove(socketChannel);
				System.out.println("Closing connection with Client... " + socketChannel.getRemoteAddress());
				socketChannel.close();
				key.cancel();
				return;
			}
			
			byte[] data = new byte[numRead];
			System.arraycopy(buffer.array(), 0, data, 0, numRead);
			System.out.println(new String(data, "UTF-8") + " from " + socketChannel.getRemoteAddress());
			doEchoJob(key, data);
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

	private void writeOP(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel)key.channel();
		
		List<byte[]> channelData = keepDataTrack.get(socketChannel);
		Iterator<byte[]> its = channelData.iterator();
		
		while (its.hasNext()) {
			byte[] it = its.next();
			its.remove();
			socketChannel.write(ByteBuffer.wrap(it));
		}
		
		key.interestOps(SelectionKey.OP_READ);
	}

	private void doEchoJob(SelectionKey key, byte[] data) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		List<byte[]> channelData = keepDataTrack.get(socketChannel);
		channelData.add(data);
		
		key.interestOps(SelectionKey.OP_WRITE);
	}
	
	public static void main(String[] args) {
		NonBlockingServer main = new NonBlockingServer();
		main.startEchoServer();
	}
}
