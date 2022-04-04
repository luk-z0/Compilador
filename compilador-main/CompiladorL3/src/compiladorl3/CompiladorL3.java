/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladorl3;

/**
 *
 * @author tarci
 */
public class CompiladorL3 {

	/**
	 * @param args the command line arguments
	 * @throws ExceptionToken 
	 */
	public static void main(String[] args) throws ExceptionToken {
		Lexico lexico = new Lexico("CompiladorL3\\src\\compiladorl3\\codigo.txt"); // IDE Eclipse
		// Lexico lexico = new Lexico("src\\compiladorl3\\codigo.txt"); //IDE Netbeans
		// Lexico lexico = new
		// Lexico("C:\\Users\\Renat\\OneDrive\\Documentos\\codigo.txt");

		Token t = null;
		while ((t = lexico.nextToken()) != null) {
			System.out.println(t.toString());
		}

	}

}
