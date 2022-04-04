/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladorl3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
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

	// Método retorna próximo token válido ou retorna mensagem de erro.
	public Token nextToken() throws ExceptionToken {
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
					} else if (c == '+' || c == '-' || c == '/' || c == '*') { // regra ARITMETICO e ++ e --
						lexema.append(c);
						estado = 8;
					} else if (c == '=') { // regra TIPO_ATRIBUICAO ou TIPO_OPERADOR_RELACIONAL
						lexema.append(c);
						estado = 9;
					} else if (c == '<' || c == '>' || c == '!') { // // TIPO_OPERADOR_RELACIONAL segunda parte
						lexema.append(c);
						estado = 10;
					} else if (c == '$') {
						lexema.append(c);
						estado = 99;
						extracted();
					} else {
						lexema.append(c);
						throw new ExceptionToken("Erro: token inválido \"" + lexema.toString() + "\"");
					}
					break;
				case 1:
					if (this.isLetra(c) || this.isDigito(c) || c == '_') {
						lexema.append(c);
						estado = 1;
					} else {
						extracted();
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
						extracted();
						return new Token(lexema.toString(), Token.TIPO_INTEIRO);
					}
					break;
				case 3:
					if (this.isDigito(c)) {
						lexema.append(c);
						estado = 4;
					} else {
						throw new ExceptionToken("Erro: número float inválido \"" + lexema.toString() + "\"");
					}
					break;
				case 4:
					if (this.isDigito(c)) {
						lexema.append(c);
						estado = 4;
					} else {
						extracted();
						return new Token(lexema.toString(), Token.TIPO_REAL);
					}
					break;
				case 5:
					extracted();
					return new Token(lexema.toString(), Token.TIPO_CARACTER_ESPECIAL);
				case 6:
					if (this.isLetra(c) || this.isDigito(c)) {
						lexema.append(c);
						estado = 6;
					} else if (c == '\'') {
						lexema.append(c);
						return new Token(lexema.toString(), Token.TIPO_CHAR);
					}
					break;
				case 7:
					if (this.isLetra(c) || this.isDigito(c) || c == ' ' || c == '\t' || c == '=' || c == '+' || c == '-'
							|| c == '*' || c == ')' || c == '(' || c == '{' || c == '}' || c == ','
							|| c == '_'
							|| c == '.' || c == ';') {
						lexema.append(c);
						estado = 7;
					} else if (c == '\n' && c != '"') {
						throw new ExceptionToken("Erro: não teve fecha aspa dupla pra finalizar o tipo string \""
								+ lexema.toString() + "\"");
					} else if (c == '"') {
						lexema.append(c);
						return new Token(lexema.toString(), Token.TIPO_STRING);
					} else if (c == '/' && this.nextChar() != '/') {
						lexema.append(c);
					} else {
						lexema.append(c);
						lexema.append(this.nextChar());
						for (; this.nextChar() >= conteudo.length; this.nextChar()) {
							while (conteudo[this.nextChar()] == '\n') {
								this.nextChar();
							}
						}
					}
					break;
				case 8:
					if (c == '+') {
						lexema.append(c);
						return new Token(lexema.toString(), Token.TIPO_SOMA_DUAS_VEZES);
					} else if (c == '-') {
						lexema.append(c);
						return new Token(lexema.toString(), Token.TIPO_SUBTRAI_DUAS_VEZES);
					} else {
						extracted();
						return new Token(lexema.toString(), Token.TIPO_OPERADOR_ARITMETICO);
					}
				case 9:
					if ((c == '\n' || c == '\t' || c == ' ' || c == '\r') && (c != '=' || c == '<' || c == '>')) {
						extracted();
						return new Token(lexema.toString(), Token.TIPO_ATRIBUICAO);
					} else if (c == '=') {
						lexema.append(c);
						return new Token(lexema.toString(), Token.TIPO_OPERADOR_RELACIONAL);

					}
					break;
				case 10:
					if (c == '\n' || c == '\t' || c == ' ' || c == '\r') {
						extracted();
						return new Token(lexema.toString(), Token.TIPO_OPERADOR_RELACIONAL);
					} else if (c == '=') {
						lexema.append(c);
						return new Token(lexema.toString(), Token.TIPO_OPERADOR_RELACIONAL);
					} else {
						throw new ExceptionToken(
								"Erro: não foi aceito nas regras do relacional \"" + lexema.toString() + "\"");
					}
				case 11:

				case 99:
					return new Token(lexema.toString(), Token.TIPO_FIM_CODIGO);
			}
		}
		return token;
	}

	private void extracted() {
		this.back();
	}
}
