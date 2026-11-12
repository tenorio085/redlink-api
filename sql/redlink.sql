-- =====================================================================
-- RedLink - Global Solution 2026/2 (CitizenTech)
-- Script de criacao das tabelas e sequences usadas pela API Java
-- Banco: Oracle FIAP
-- Padrao: tb_ (tabela), cd_ (codigo), nm_ (nome), ds_ (descricao),
--         dt_ (data), nr_ (numero), sg_ (sigla), sq_tb_ (sequence)
-- =====================================================================

-- Para recriar tudo do zero, rode antes (nesta ordem):
--   DROP TABLE tb_rl_doacao;
--   DROP TABLE tb_rl_doador;
--   DROP TABLE tb_rl_hemocentro;
--   DROP TABLE tb_rl_tipo_sanguineo;
--   DROP SEQUENCE sq_tb_rl_doacao;
--   DROP SEQUENCE sq_tb_rl_doador;
--   DROP SEQUENCE sq_tb_rl_hemocentro;

-- ---------------------------------------------------------------------
-- Tipos sanguineos (tabela de dominio, 8 linhas fixas)
-- ---------------------------------------------------------------------
CREATE TABLE tb_rl_tipo_sanguineo (
    cd_tipo NUMBER(2)   NOT NULL,
    ds_tipo VARCHAR2(3) NOT NULL,
    CONSTRAINT pk_rl_tipo_sanguineo PRIMARY KEY (cd_tipo),
    CONSTRAINT uk_rl_tipo_ds UNIQUE (ds_tipo)
);

-- ---------------------------------------------------------------------
-- Hemocentros
-- ---------------------------------------------------------------------
CREATE TABLE tb_rl_hemocentro (
    cd_hemocentro NUMBER(10)    NOT NULL,
    nm_hemocentro VARCHAR2(100) NOT NULL,
    ds_endereco   VARCHAR2(200) NOT NULL,
    nm_cidade     VARCHAR2(80)  NOT NULL,
    CONSTRAINT pk_rl_hemocentro PRIMARY KEY (cd_hemocentro)
);

-- ---------------------------------------------------------------------
-- Doadores (N doadores : 1 tipo sanguineo)
-- ---------------------------------------------------------------------
CREATE TABLE tb_rl_doador (
    cd_doador     NUMBER(10)    NOT NULL,
    nm_doador     VARCHAR2(100) NOT NULL,
    nr_cpf        VARCHAR2(11)  NOT NULL,
    sg_sexo       CHAR(1)       NOT NULL,
    dt_nascimento DATE          NOT NULL,
    nr_peso       NUMBER(5,2)   NOT NULL,
    ds_email      VARCHAR2(120) NOT NULL,
    nr_telefone   VARCHAR2(15),
    cd_tipo       NUMBER(2)     NOT NULL,
    CONSTRAINT pk_rl_doador PRIMARY KEY (cd_doador),
    CONSTRAINT uk_rl_doador_cpf UNIQUE (nr_cpf),
    CONSTRAINT ck_rl_doador_sexo CHECK (sg_sexo IN ('M', 'F')),
    CONSTRAINT ck_rl_doador_peso CHECK (nr_peso >= 50),
    CONSTRAINT fk_rl_doador_tipo FOREIGN KEY (cd_tipo) REFERENCES tb_rl_tipo_sanguineo (cd_tipo)
);

-- ---------------------------------------------------------------------
-- Doacoes (relacionamento 1:N -> um doador tem varias doacoes)
-- ---------------------------------------------------------------------
CREATE TABLE tb_rl_doacao (
    cd_doacao     NUMBER(10)   NOT NULL,
    dt_doacao     DATE         NOT NULL,
    ds_status     VARCHAR2(20) NOT NULL,
    cd_doador     NUMBER(10)   NOT NULL,
    cd_hemocentro NUMBER(10)   NOT NULL,
    CONSTRAINT pk_rl_doacao PRIMARY KEY (cd_doacao),
    CONSTRAINT ck_rl_doacao_status CHECK (ds_status IN ('AGENDADA', 'REALIZADA', 'NAO_COMPARECEU', 'CANCELADA')),
    CONSTRAINT fk_rl_doacao_doador FOREIGN KEY (cd_doador) REFERENCES tb_rl_doador (cd_doador),
    CONSTRAINT fk_rl_doacao_hemocentro FOREIGN KEY (cd_hemocentro) REFERENCES tb_rl_hemocentro (cd_hemocentro)
);

-- ---------------------------------------------------------------------
-- Sequences (uma por tabela com chave gerada)
-- ---------------------------------------------------------------------
CREATE SEQUENCE sq_tb_rl_hemocentro START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_rl_doador     START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_rl_doacao     START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- Carga inicial: os 8 tipos sanguineos (obrigatoria para cadastrar doadores)
-- ---------------------------------------------------------------------
INSERT INTO tb_rl_tipo_sanguineo (cd_tipo, ds_tipo) VALUES (1, 'A+');
INSERT INTO tb_rl_tipo_sanguineo (cd_tipo, ds_tipo) VALUES (2, 'A-');
INSERT INTO tb_rl_tipo_sanguineo (cd_tipo, ds_tipo) VALUES (3, 'B+');
INSERT INTO tb_rl_tipo_sanguineo (cd_tipo, ds_tipo) VALUES (4, 'B-');
INSERT INTO tb_rl_tipo_sanguineo (cd_tipo, ds_tipo) VALUES (5, 'AB+');
INSERT INTO tb_rl_tipo_sanguineo (cd_tipo, ds_tipo) VALUES (6, 'AB-');
INSERT INTO tb_rl_tipo_sanguineo (cd_tipo, ds_tipo) VALUES (7, 'O+');
INSERT INTO tb_rl_tipo_sanguineo (cd_tipo, ds_tipo) VALUES (8, 'O-');

-- Carga de exemplo (dados ficticios): dois hemocentros
INSERT INTO tb_rl_hemocentro (cd_hemocentro, nm_hemocentro, ds_endereco, nm_cidade)
VALUES (sq_tb_rl_hemocentro.NEXTVAL, 'Hemocentro Central', 'Av. Dr. Eneas Carvalho de Aguiar, 155', 'Sao Paulo');
INSERT INTO tb_rl_hemocentro (cd_hemocentro, nm_hemocentro, ds_endereco, nm_cidade)
VALUES (sq_tb_rl_hemocentro.NEXTVAL, 'Hemocentro Zona Leste', 'Rua das Flores, 1200', 'Sao Paulo');

COMMIT;
