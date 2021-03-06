/*
 * Copyright (c) 2018-2019 Karlatemp. All rights reserved.
 * Reserved.FileName: RconClient.java@author: karlatemp@vip.qq.com: 19-9-18 下午5:54@version: 2.0
 */

package cn.mcres.gyhhy.MXLib.http.handlers.rcon;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An RCON client to remotely control a Minecraft server. Use
 * {@link #open(String, int, String)} to create an instance of
 * {@link RconClient}, open a connection to a Minecraft server, and
 * authenticate. Then use {@link #sendCommand(String)} to send a command to the
 * server. Make sure to close the connection again using {@link #close()}.
 * <p>
 * The connection is only opened when the client is created. If for any reason
 * the connection is closed or broken (eg. by the server shutting down or some
 * network failure), no attempt is made to re-establish the connection. When
 * {@code sendCommand} is invoked in such a case, an exception will occur.
 * <p>
 * To enable RCON support in your Minecraft server, add the following to your
 * {@code server.properties} (or modify existing properties if they are already
 * present):
 * <pre>{@code
 * enable-rcon=true
 * rcon.password=<your password>
 * rcon.port=<1-65535>
 * }</pre>
 *
 * @see <a href="http://wiki.vg/RCON">Page describing the RCON protocol</a>
 * @see <a href="http://minecraft.gamepedia.com/Server.properties">Minecraft
 * server.properties documentation</a>
 */
public class RconClient implements Closeable {

    protected static final int AUTHENTICATION_FAILURE_ID = -1;
    protected static final Charset DEFAULT_PAYLOAD_CHARSET = StandardCharsets.UTF_8;
    protected static final int TYPE_COMMAND = 2;
    protected static final int TYPE_AUTH = 3;

    /**
     * Create an instance of {@link RconClient}, opening a connection to the
     * specified {@code host} and {@code port}, and authenticate using the
     * specified {@code password}. If no connection can be established,
     * {@link RconClientException} is thrown, wrapping any exception of the
     * underlying communication channel (eg. {@link IOException}). If the
     * {@code password} is incorrect, an {@link AuthFailureException} is thrown.
     *
     * @param host The server's host name or IP address
     * @param port The server RCON port number ({@code rcon.port} in
     * {@code server.properties})
     * @param password The server's RCON password ({@code rcon.password} in
     * {@code server.properties})
     * @return An {@link RconClient} with an established connection
     * @throws RconClientException When any exception is thrown by the
     * communication channel
     * @throws AuthFailureException When the password is wrong
     */
    public static RconClient open(String host, int port, String password) throws IOException {
        SocketChannel socketChannel;
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        } catch (IOException e) {
            throw new RconClientException("Failed to open socket to " + host + ":" + port, e);
        }

        RconClient rconClient = new RconClient(socketChannel);
        try {
            rconClient.authenticate(password);
        } catch (Exception authException) {
            try {
                rconClient.close();
            } catch (Exception closingException) {
                authException.addSuppressed(closingException);
            }
            throw authException;
        }
        return rconClient;
    }
    protected Charset PAYLOAD_CHARSET = DEFAULT_PAYLOAD_CHARSET;

    protected final SocketChannel socketChannel;
    protected final AtomicInteger currentRequestId;

    protected boolean isAutoRead = true;

    protected RconClient(SocketChannel socketChannel) {
        this.socketChannel = Objects.requireNonNull(socketChannel, "socketChannel");
        this.currentRequestId = new AtomicInteger(1);
    }

    public Charset getCharset() {
        return PAYLOAD_CHARSET;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public RconClient setCharset(Charset c) {
        PAYLOAD_CHARSET = Objects.requireNonNull(c, "charset");
        return this;
    }

    public boolean autoRead() {
        return isAutoRead;
    }

    public void setAutoRead(boolean r) {
        isAutoRead = r;
    }

    /**
     * Send {@code command} to the server, returning any data that was returned
     * by the server. Note that in a lot of cases when the command is delivered
     * and executed successfully, an empty response is returned by the server,
     * resulting in an empty String as a return value of this method. When an
     * unknown command is sent, the server will return some text along the lines
     * of {@code "Unknown command. Try /help for a list of commands"}.
     * <p>
     * When any communication failure occurs (eg. broken connection, server has
     * shut down), an {@link RconClientException} is thrown, wrapping any
     * exception of the underlying communication channel (eg.
     * {@link IOException}).
     *
     * @param command The command to send to the server
     * @return Response as returned by the server
     * @throws RconClientException When any exception is thrown by the
     * communication channel
     */
    public String sendCommand(String command) throws IOException {
        return send(TYPE_COMMAND, command);
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            socketChannel.close();
        } catch (IOException e) {
            throw new RconClientException("Failed to close socket channel", e);
        }
    }

    public boolean isOpen() {
        return socketChannel.isOpen();
    }

    protected void authenticate(String password) throws IOException {
        send(TYPE_AUTH, password);
    }

    protected synchronized String send(int type, String payload) throws IOException {
        return send(type, payload.getBytes(PAYLOAD_CHARSET));
    }

    protected synchronized String send(int type, byte[] payload) throws IOException {
        return new String(send_(type, payload), PAYLOAD_CHARSET);
    }

    protected synchronized byte[] send_(int type, byte[] payload) throws IOException {
        return send_(type, payload, 0, payload.length);
    }

    protected synchronized byte[] send_(int type, byte[] payload, int from, int length) throws IOException {
        int requestId = currentRequestId.getAndIncrement();

        ByteBuffer buffer = toByteBuffer(requestId, type, payload, from, length);
        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
            throw new RconClientException("Failed to write " + buffer.capacity() + " bytes", e);
        }
        if (!isAutoRead) {
            return null;
        }
        ByteBuffer responseBuffer = readResponse();
        int responseId = responseBuffer.getInt();

        if (responseId == AUTHENTICATION_FAILURE_ID) {
            throw new AuthFailureException();
        }

        if (responseId != requestId) {
            throw new RconClientException("Sent request id " + requestId + " but received " + responseId);
        }

        @SuppressWarnings("unused")
        int responseType = responseBuffer.getInt();

        byte[] bodyBytes = new byte[responseBuffer.remaining()];
        responseBuffer.get(bodyBytes);
        return bodyBytes;
    }

    public synchronized ByteBuffer readResponse() throws IOException {
        int size = readData(Integer.BYTES).getInt();
        ByteBuffer dataBuffer = readData(size - (2 * Byte.BYTES));
        ByteBuffer nullsBuffer = readData(2 * Byte.BYTES);

        byte null1 = nullsBuffer.get(0);
        byte null2 = nullsBuffer.get(1);

        if (null1 != 0 || null2 != 0) {
            throw new RconClientException("Expected 2 null bytes but received " + null1 + " and " + null2);
        }

        return dataBuffer;
    }

    protected synchronized ByteBuffer readData(int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        int readCount;
        try {
            readCount = socketChannel.read(buffer);
        } catch (IOException e) {
            throw new RconClientException("Failed to read " + size + " bytes", e);
        }

        if (readCount != size) {
            throw new RconClientException("Expected " + size + " bytes but received " + readCount);
        }

        buffer.position(0);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    protected ByteBuffer toByteBuffer(int requestId, int type, byte[] payload, int from, int length) {
        if (length == 0) {
            length = payload.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate((3 * Integer.BYTES) + length + (2 * Byte.BYTES));
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt((2 * Integer.BYTES) + length + (2 * Byte.BYTES));
        buffer.putInt(requestId);
        buffer.putInt(type);
//        System.out.println(new String(payload, from, length));
        buffer.put(payload, from, length);
        buffer.put((byte) 0);
        buffer.put((byte) 0);

        buffer.position(0);
        return buffer;
    }

    protected ByteBuffer toByteBuffer(int requestId, int type, String payload) {
        return toByteBuffer(requestId, type, payload.getBytes(PAYLOAD_CHARSET), 0, 0);
    }
}
