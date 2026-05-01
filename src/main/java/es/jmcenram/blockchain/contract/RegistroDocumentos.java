package es.jmcenram.blockchain.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.10.3.
 */
@SuppressWarnings("rawtypes")
public class RegistroDocumentos extends Contract {
    public static final String BINARY = "6080604052348015600e575f5ffd5b5061061f8061001c5f395ff3fe608060405234801561000f575f5ffd5b5060043610610060575f3560e01c80630845567e14610064578063148b7910146100795780632c5b066d14610097578063681161a5146100d057806390eb3b5e146100f3578063f926afc014610106575b5f5ffd5b610077610072366004610515565b610129565b005b61008161020c565b60405161008e919061052c565b60405180910390f35b6100c36100a5366004610515565b5f90815260016020526040902060020154600160a01b900460ff1690565b60405161008e91906105a2565b6100e36100de366004610515565b610261565b604051901515815260200161008e565b610077610101366004610515565b610298565b610119610114366004610515565b610417565b60405161008e94939291906105b6565b60015f828152600160205260409020600290810154600160a01b900460ff16908111156101585761015861056e565b146101a05760405162461bcd60e51b815260206004820152601360248201527227379039b290383ab2b232903932bb37b1b0b960691b60448201526064015b60405180910390fd5b5f8181526001602052604090206002908101805460ff60a01b1916600160a01b8302179055506040805182815233602082015242918101919091527f804143cab8ebb821bbcc70f331f1429dd0cfa45fa12871c151c862fc1a9759d3906060015b60405180910390a150565b60605f80548060200260200160405190810160405280929190818152602001828054801561025757602002820191905f5260205f20905b815481526020019060010190808311610243575b5050505050905090565b5f60015f838152600160205260409020600290810154600160a01b900460ff16908111156102915761029161056e565b1492915050565b5f5f828152600160205260409020600290810154600160a01b900460ff16908111156102c6576102c661056e565b146103135760405162461bcd60e51b815260206004820152601860248201527f5961207265676973747261646f206f207265766f6361646f00000000000000006044820152606401610197565b60408051608081018252828152426020808301918252338385019081526001606085018181525f88815293829052959092208451815592519183019190915551600280830180546001600160a01b039093166001600160a01b03198416811782559551949593949390926001600160a81b0319161790600160a01b9084908111156103a0576103a061056e565b0217905550505f80546001810182559080527f290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e56301829055506040805182815233602082015242918101919091527f58494447ccaf24c66341a91ba7887a3125c3b04665010da91945e31a39991c4d90606001610201565b5f808080805f868152600160205260409020600290810154600160a01b900460ff16908111156104495761044961056e565b036104825760405162461bcd60e51b81526020600482015260096024820152684e6f2065786973746560b81b6044820152606401610197565b5f85815260016020818152604080842081516080810183528154815293810154928401929092526002808301546001600160a01b038116928501929092526060840191600160a01b900460ff16908111156104df576104df61056e565b60028111156104f0576104f061056e565b9052508051602082015160408301516060909301519199909850919650945092505050565b5f60208284031215610525575f5ffd5b5035919050565b602080825282518282018190525f918401906040840190835b81811015610563578351835260209384019390920191600101610545565b509095945050505050565b634e487b7160e01b5f52602160045260245ffd5b6003811061059e57634e487b7160e01b5f52602160045260245ffd5b9052565b602081016105b08284610582565b92915050565b848152602081018490526001600160a01b0383166040820152608081016105e06060830184610582565b9594505050505056fea2646970667358221220a683e03e24680f04f4c013c54b1d775cbb9b9f85dcab20db66a26fa546d86de164736f6c634300081e0033";

    public static final String FUNC_OBTENERDOCUMENTO = "obtenerDocumento";

    public static final String FUNC_OBTENERESTADO = "obtenerEstado";

    public static final String FUNC_OBTENERTODOSHASHES = "obtenerTodosHashes";

    public static final String FUNC_REGISTRARDOCUMENTO = "registrarDocumento";

    public static final String FUNC_REVOCARDOCUMENTO = "revocarDocumento";

    public static final String FUNC_VERIFICARDOCUMENTO = "verificarDocumento";

    public static final Event DOCUMENTOREGISTRADO_EVENT = new Event("DocumentoRegistrado", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event DOCUMENTOREVOCADO_EVENT = new Event("DocumentoRevocado", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected RegistroDocumentos(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected RegistroDocumentos(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected RegistroDocumentos(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected RegistroDocumentos(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<DocumentoRegistradoEventResponse> getDocumentoRegistradoEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DOCUMENTOREGISTRADO_EVENT, transactionReceipt);
        ArrayList<DocumentoRegistradoEventResponse> responses = new ArrayList<DocumentoRegistradoEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DocumentoRegistradoEventResponse typedResponse = new DocumentoRegistradoEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.hash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.registrador = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.fecha = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DocumentoRegistradoEventResponse getDocumentoRegistradoEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DOCUMENTOREGISTRADO_EVENT, log);
        DocumentoRegistradoEventResponse typedResponse = new DocumentoRegistradoEventResponse();
        typedResponse.log = log;
        typedResponse.hash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.registrador = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.fecha = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<DocumentoRegistradoEventResponse> documentoRegistradoEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDocumentoRegistradoEventFromLog(log));
    }

    public Flowable<DocumentoRegistradoEventResponse> documentoRegistradoEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DOCUMENTOREGISTRADO_EVENT));
        return documentoRegistradoEventFlowable(filter);
    }

    public static List<DocumentoRevocadoEventResponse> getDocumentoRevocadoEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(DOCUMENTOREVOCADO_EVENT, transactionReceipt);
        ArrayList<DocumentoRevocadoEventResponse> responses = new ArrayList<DocumentoRevocadoEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DocumentoRevocadoEventResponse typedResponse = new DocumentoRevocadoEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.hash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.revocador = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.fecha = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static DocumentoRevocadoEventResponse getDocumentoRevocadoEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(DOCUMENTOREVOCADO_EVENT, log);
        DocumentoRevocadoEventResponse typedResponse = new DocumentoRevocadoEventResponse();
        typedResponse.log = log;
        typedResponse.hash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.revocador = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.fecha = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<DocumentoRevocadoEventResponse> documentoRevocadoEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDocumentoRevocadoEventFromLog(log));
    }

    public Flowable<DocumentoRevocadoEventResponse> documentoRevocadoEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DOCUMENTOREVOCADO_EVENT));
        return documentoRevocadoEventFlowable(filter);
    }

    public RemoteFunctionCall<Tuple4<byte[], BigInteger, String, BigInteger>> obtenerDocumento(byte[] _hash) {
        final Function function = new Function(FUNC_OBTENERDOCUMENTO, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_hash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Uint8>() {}));
        return new RemoteFunctionCall<Tuple4<byte[], BigInteger, String, BigInteger>>(function,
                new Callable<Tuple4<byte[], BigInteger, String, BigInteger>>() {
                    @Override
                    public Tuple4<byte[], BigInteger, String, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple4<byte[], BigInteger, String, BigInteger>(
                                (byte[]) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue());
                    }
                });
    }

    public RemoteFunctionCall<BigInteger> obtenerEstado(byte[] _hash) {
        final Function function = new Function(FUNC_OBTENERESTADO, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_hash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<List> obtenerTodosHashes() {
        final Function function = new Function(FUNC_OBTENERTODOSHASHES, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> registrarDocumento(byte[] _hash) {
        final Function function = new Function(
                FUNC_REGISTRARDOCUMENTO, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_hash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> revocarDocumento(byte[] _hash) {
        final Function function = new Function(
                FUNC_REVOCARDOCUMENTO, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_hash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> verificarDocumento(byte[] _hash) {
        final Function function = new Function(FUNC_VERIFICARDOCUMENTO, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(_hash)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    @Deprecated
    public static RegistroDocumentos load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new RegistroDocumentos(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static RegistroDocumentos load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new RegistroDocumentos(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static RegistroDocumentos load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new RegistroDocumentos(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static RegistroDocumentos load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new RegistroDocumentos(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<RegistroDocumentos> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(RegistroDocumentos.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<RegistroDocumentos> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(RegistroDocumentos.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<RegistroDocumentos> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(RegistroDocumentos.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<RegistroDocumentos> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(RegistroDocumentos.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class DocumentoRegistradoEventResponse extends BaseEventResponse {
        public byte[] hash;

        public String registrador;

        public BigInteger fecha;
    }

    public static class DocumentoRevocadoEventResponse extends BaseEventResponse {
        public byte[] hash;

        public String revocador;

        public BigInteger fecha;
    }
}
