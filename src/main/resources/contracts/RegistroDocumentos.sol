// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract RegistroDocumentos {

    enum Estado {
        NO_EXISTE,
        REGISTRADO,
        REVOCADO
    }

    struct Documento {
        bytes32 hash;
        uint256 fechaRegistro;
        address registrador;
        Estado estado;
    }

    bytes32[] private listaHashes;

    mapping(bytes32 => Documento) private documentos;

    event DocumentoRegistrado(
        bytes32 hash,
        address registrador,
        uint256 fecha
    );

    event DocumentoRevocado(
        bytes32 hash,
        address revocador,
        uint256 fecha
    );

    // =========================
    // =========================
    function registrarDocumento(bytes32 _hash) public {

        require(
            documentos[_hash].estado == Estado.NO_EXISTE,
            "Ya registrado o revocado"
        );

        documentos[_hash] = Documento({
            hash: _hash,
            fechaRegistro: block.timestamp,
            registrador: msg.sender,
            estado: Estado.REGISTRADO
        });

        listaHashes.push(_hash);

        emit DocumentoRegistrado(_hash, msg.sender, block.timestamp);
    }

    // =========================
    // =========================
    function revocarDocumento(bytes32 _hash) public {

        require(
            documentos[_hash].estado == Estado.REGISTRADO,
            "No se puede revocar"
        );

        documentos[_hash].estado = Estado.REVOCADO;

        emit DocumentoRevocado(_hash, msg.sender, block.timestamp);
    }

    // =========================
    // =========================
    function verificarDocumento(bytes32 _hash) public view returns (bool) {
        return documentos[_hash].estado == Estado.REGISTRADO;
    }

    // =========================
    // =========================
    function obtenerEstado(bytes32 _hash) public view returns (Estado) {
        return documentos[_hash].estado;
    }

    // =========================
    // =========================
    function obtenerTodosHashes() public view returns (bytes32[] memory) {
        return listaHashes;
    }

    // =========================
    // =========================
    function obtenerDocumento(bytes32 _hash)
    public
    view
    returns (bytes32, uint256, address, Estado)
    {
        require(
            documentos[_hash].estado != Estado.NO_EXISTE,
            "No existe"
        );

        Documento memory doc = documentos[_hash];

        return (
            doc.hash,
            doc.fechaRegistro,
            doc.registrador,
            doc.estado
        );
    }
}