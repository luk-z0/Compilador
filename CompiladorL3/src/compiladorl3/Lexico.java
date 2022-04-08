/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladorl3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author tarci
 */
public class Lexico {
    private char[] conteudo;
    private int indiceConteudo;

    public Lexico(String caminhoCodigoFonte) {
        try {
            String conteudoStr;
            conteudoStr = new String(Files.readAllBytes(Paths.get(caminhoCodigoFonte)));
            this.conteudo = conteudoStr.toCharArray();
            this.indiceConteudo = 0;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Retorna próximo char
    private char nextChar() {
        return this.conteudo[this.indiceConteudo++];
    }

    // Verifica existe próximo char ou chegou ao final do código fonte
    private boolean hasNextChar() {
        return indiceConteudo < this.conteudo.length;
    }

    // Retrocede o índice que aponta para o "char da vez" em uma unidade
    private void back() {
        this.indiceConteudo--;
    }

    // Identificar se char é letra minúscula
    private boolean isLetra(char c) {
        return (c >= 'a') && (c <= 'z');
    }

    // Identificar se char é dígito
    private boolean isDigito(char c) {
        return (c >= '0') && (c <= '9');
    }

    private void pularLinha(StringBuffer lexema) {
        lexema.append('\n');
    }


    // Método retorna próximo token válido ou retorna mensagem de erro.
    public Token nextToken() {
        Token token = null;
        char c;
        int estado = 0;

        StringBuffer lexema = new StringBuffer();
        while (this.hasNextChar()) {
            c = this.nextChar();
            switch (estado) {
                case 0:
                    if (c == ' ' || c == '\t' || c == '\n' || c == '\r') { // caracteres de espaço em branco ASCII
                        // tradicionais
                        estado = 0;
                    } else if (this.isLetra(c) || c == '_') {
                        lexema.append(c);
                        estado = 1;
                    } else if (this.isDigito(c)) {
                        lexema.append(c);
                        estado = 2;
                    } else if (c == ')' || c == '(' || c == '{' || c == '}' || c == ',' || c == ';') {
                        lexema.append(c);
                        estado = 5;
                    } else if (c == '\'') { // regra do char
                        lexema.append(c);
                        estado = 6;
                    } else if (c == '"') { // regra da string add n°1
                        lexema.append(c);
                        estado = 7;
                    } else if (c == '+' || c == '-' || c == '/' || c == '*') { // regra ARITMETICO
                      if (this.nextChar() == '/') {
                            this.back();
                            if (this.nextChar() != '\n') {
                                this.nextChar();
                            }
                            lexema.append(c);
                            lexema.append(this.nextChar());
                            estado = 10;
                            break;
                        }
                        lexema.append(c);
                        estado = 8;
                    } else if (c == '=') { // regra TIPO_ATRIBUICAO
                        lexema.append(c);
                        estado = 9;
                    } else if (c == '$') {
                        lexema.append(c);
                        estado = 99;
                        this.back();
                    } else {
                        lexema.append(c);
                        throw new RuntimeException("Erro: token inválido \"" + lexema + "\"");
                    }
                    break;
                case 1:
                    if (this.isLetra(c) || this.isDigito(c) || c == '_') {
                        lexema.append(c);
                        estado = 1;
                    } else {
                        this.back();
                        return new Token(lexema.toString(), Token.TIPO_IDENTIFICADOR);
                    }
                    break;
                case 2:
                    if (this.isDigito(c)) {
                        lexema.append(c);
                        estado = 2;
                    } else if (c == '.') {
                        lexema.append(c);
                        estado = 3;
                    } else {
                        this.back();
                        return new Token(lexema.toString(), Token.TIPO_INTEIRO);
                    }
                    break;
                case 3:
                    if (this.isDigito(c)) {
                        lexema.append(c);
                        estado = 4;
                    } else {
                        throw new RuntimeException("Erro: número float inválido \"" + lexema + "\"");
                    }
                    break;
                case 4:
                    if (this.isDigito(c)) {
                        lexema.append(c);
                        estado = 4;
                    } else {
                        this.back();
                        return new Token(lexema.toString(), Token.TIPO_REAL);
                    }
                    break;
                case 5:
                    this.back();
                    return new Token(lexema.toString(), Token.TIPO_CARACTER_ESPECIAL);
                case 6:
                    if (this.isLetra(c) || this.isDigito(c)) {
                        lexema.append(c);
                        estado = 6;
                    } else if (c == '\'') {
                        lexema.append(c);
                        this.back();
                        if (this.isLetra(c) || this.isDigito(c)) {
                            this.back();
                            if (c == '\'') {
                                return new Token(lexema.toString(), Token.TIPO_CHAR);
                            } else {
                                throw new RuntimeException("Erro: não atendendo a regra de character unico entre aspas simples \"" + lexema.toString() + "\"");
                            }
                        } else {
                            throw new RuntimeException("Erro: não atendendo a regra de character unico entre apsas simples \"" + lexema + "\"");
                        }
                    }
                    break;
                case 7:
                    if (this.isLetra(c) || this.isDigito(c) || c == ' ' || c == '\t' || c == '=' || c == '+' || c == '-' || c == '*' || c == '/' || c == ')' || c == '(' || c == '{' || c == '}' || c == ',' || c == '_' || c == '.' || c == ';') {
                        lexema.append(c);
                        estado = 7;
                    } else if (c == '\n' && c != '"') {
                        throw new RuntimeException("Erro: não teve fecha aspa dupla pra finalizar o tipo string \"" + lexema + "\"");
                    } else if (c == '"') {
                        lexema.append(c);
                        return new Token(lexema.toString(), Token.TIPO_STRING);
                    }
                    break;
                case 8:
                    this.back();
                    return new Token(lexema.toString(), Token.TIPO_OPERADOR_ARITMETICO);

                case 9:
                    this.back();
                    return new Token(lexema.toString(), Token.TIPO_ATRIBUICAO);
                case 10:
                    return new Token(lexema.toString(), Token.TIPO_COMENTARIO);
                case 99:
                    return new Token(lexema.toString(), Token.TIPO_FIM_CODIGO);
            }
        }
        return token;
    }
}
