package com.eternitywall.ots;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Represents a (possibly local) Bitcoin node which we can ask for block hashes and headers
 */
public class BitcoinNode {

    private String authString;
    private String urlString;

    private static String RPCCONNECT = "rpcconnect";
    private static String RPCUSER = "rpcuser";
    private static String RPCPORT = "rpcport";
    private static String RPCPASSWORD = "rpcpassword";

    public BitcoinNode(Properties bitcoinConf) {
        authString = String.valueOf(Base64Coder.encode(String.format("%s:%s", bitcoinConf.getProperty(RPCUSER), bitcoinConf.getProperty(RPCPASSWORD)).getBytes()));
        urlString = String.format("http://%s:%s", bitcoinConf.getProperty(RPCCONNECT), bitcoinConf.getProperty(RPCPORT));
    }

    public static Properties readBitcoinConf() throws Exception {
        String home = System.getProperty("user.home");
        List<String> dirs = Arrays.asList("/.bitcoin/", "\\AppData\\Roaming\\Bitcoin\\", "/Library/Application Support/Bitcoin/");

        for (String dir : dirs) {
            Properties prop = new Properties();
            InputStream input = null;

            try {
                input = new FileInputStream(home + dir + "bitcoin.conf");

                prop.load(input);

                // If we have a RPC user and password, make sure we set RPCCONNECT and RPCPORT, if missing
                if (prop.getProperty(RPCUSER) != null && prop.getProperty(RPCPASSWORD) != null) {
                    if (prop.getProperty(RPCCONNECT) == null) {
                        prop.setProperty(RPCCONNECT, "127.0.0.1");
                    }

                    if (prop.getProperty(RPCPORT) == null) {
                        prop.setProperty(RPCPORT, "8332");
                    }

                    return prop;
                }
            } catch (IOException ex) {
                // This is expected for all the paths to bitcoin.conf that doesn't exist on this particular machine
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        // Could not close input stream. Ignore issue.
                    }
                }
            }
        }

        throw new Exception("No bitcoin.conf file found in any of these paths: " + Arrays.toString(dirs.toArray()));
    }

    public JSONObject getBlockChainInfo() throws Exception {
        JSONObject json = new JSONObject();
        json.put("method", "getblockchaininfo");

        return callRPC(json);
    }

    public BlockHeader getBlockHeader(Integer height) throws Exception {
        return getBlockHeader(getBlockHash(height));
    }

    public BlockHeader getBlockHeader(String hash) throws Exception {
        if (hash == null) {
            return null;      // TODO: I think this will result in strange failures later on. Throw instead?
        }

        JSONObject json = new JSONObject();
        json.put("method", "getblockheader");
        JSONArray array = new JSONArray();
        array.put(hash);
        json.put("params", array);
        JSONObject jsonObject = callRPC(json);
        BlockHeader blockHeader = new BlockHeader();
        JSONObject result = jsonObject.getJSONObject("result");
        blockHeader.setMerkleroot(result.getString("merkleroot"));
        blockHeader.setBlockHash(hash);
        blockHeader.setTime(String.valueOf(result.getInt("time")));

        return blockHeader;
    }

    public String getBlockHash(Integer height) throws Exception {
        JSONObject json = new JSONObject();
        json.put("method", "getblockhash");
        JSONArray array = new JSONArray();
        array.put(height);
        json.put("params", array);
        JSONObject jsonObject = callRPC(json);

        return jsonObject.getString("result");
    }

    private JSONObject callRPC(JSONObject query) throws Exception {
        String s = query.toString();
        URL url = new URL(urlString);
        Request request = new Request(url);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + authString);
        request.setHeaders(headers);
        request.setData(s.getBytes());
        Response response = request.call();

        if (response == null) {
            throw new Exception("Could not get response from " + urlString);
        }

        return new JSONObject(response.getString());
    }
}
