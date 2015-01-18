package pl.mcybulski.XMLConfig;


import pl.mcybulski.XMLConfig.commons.Token;
import pl.mcybulski.XMLConfig.commons.TokenType;
import pl.mcybulski.XMLConfig.exceptions.UnrecognizedTokenException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by Mikołaj on 2014-12-22.
 */
public class Scanner {

    public static final char NO_NEXT_LOGICAL_SIGN = 'E';
    private FileInputStream fileInputStream;
    private String fileContent;


    public Token getNextToken() throws UnrecognizedTokenException {
        try {
            Token ret = readXMLSTag();
            if (ret != null) {
                return ret;
            }
            ret = readXMLETag();

            if (ret != null) {
                return ret;
            }
            ret = readVersionAttribute();

            if (ret != null) {
                return ret;
            }

            ret = readSTag();

            if (ret != null) {
                return ret;
            }
            ret = readTypeAttribute();

            if (ret != null) {
                return ret;
            }
            ret = readArrayAttribute();

            if (ret != null) {
                return ret;
            }
            ret = readEquals();

            if (ret != null) {
                return ret;
            }
            ret = readType();

            if (ret != null) {
                return ret;
            }
            ret = readVersion();

            if (ret != null) {
                return ret;
            }
            ret = readTagName();

            if (ret != null) {
                return ret;
            }
            ret = readETag();

            if (ret != null) {
                return ret;
            }
            ret = readValue();

            if (ret != null) {
                return ret;
            }
            ret = readSlash();

            if (ret != null) {
                return ret;
            }
        } catch (IndexOutOfBoundsException e) {
            if (fileContent.trim().length() == 0) {
                return new Token(TokenType.EOF, "");
            } else {
                throw new UnrecognizedTokenException();
            }
        }

        if (fileContent.trim().length() == 0) {
            return new Token(TokenType.EOF, "");
        } else {
            throw new UnrecognizedTokenException();
        }

    }

    private Token readSlash() {
        if (fileContent.charAt(0) == '/') {
            fileContent = fileContent.substring(1);
            return new Token(TokenType.SLASH, "/");
        }
        return null;
    }

    private Token readValue() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i + 1 < fileContent.length(); i++) {
            if(fileContent.charAt(i) == '>' || fileContent.charAt(i) == '&'){
                return null;
            }
            if (fileContent.charAt(i) == '<' && fileContent.charAt(i + 1) == '/') {
                fileContent = fileContent.substring(i);
                return new Token(TokenType.VALUE, stringBuilder.toString());
            }
            stringBuilder.append(fileContent.charAt(i));
        }

        return null;
    }

    private Token readType() {
        int charNo = getFirstSymbolNo();
        String[] allowedTypes = {"string" , "int" , "double" , "float" , "long" , "null" , "boolean"};

        if (fileContent.charAt(charNo) == '\"') {
            for (int i = 0; i < allowedTypes.length; i++) {
                String allowedType = allowedTypes[i];

                if (fileContent.startsWith(allowedType, charNo + 1) && fileContent.charAt(charNo + allowedType.length() + 1) == '\"') {
                    fileContent = fileContent.substring(charNo + allowedType.length() + 2);
                    return new Token(TokenType.TYPE, allowedType);
                }
            }
        } else if (fileContent.charAt(charNo) == '\'') {
            for (int i = 0; i < allowedTypes.length; i++) {
                String allowedType = allowedTypes[i];

                if (fileContent.startsWith(allowedType, charNo + 1) && fileContent.charAt(charNo + allowedType.length() + 1) == '\'') {
                    fileContent = fileContent.substring(charNo + allowedType.length() + 2);
                    return new Token(TokenType.TYPE, allowedType);
                }
            }
        }

        return null;
    }

    private Token readEquals() {
        int charNo = getFirstSymbolNo();

        if (fileContent.charAt(charNo) == '=') {
            fileContent = fileContent.substring(++charNo);
            return new Token(TokenType.EQUALS, "=");
        }
        return null;
    }

    private Token readTypeAttribute() {
        int charNo = getFirstSymbolNo();
        String type = "type";

        if ( !(fileContent.startsWith(type, charNo) && nextLogicalSign(charNo) == '=') )  {
            return null;
        }

        fileContent = fileContent.substring(charNo + type.length());

        return new Token(TokenType.TYPE_ATTRIBUTE, type);
    }

    private Token readArrayAttribute() {
        int charNo = getFirstSymbolNo();
        String array = "array";

        if ( !(fileContent.startsWith(array, charNo) && nextLogicalSign(charNo) == '=') )  {
            return null;
        }

        fileContent = fileContent.substring(charNo + array.length());

        return new Token(TokenType.ARRAY_ATTRIBUTE, array);
    }

    private Token readTagName() {
        int charNo = getFirstSymbolNo();
        StringBuilder stringBuilder = new StringBuilder();

        if ( !(Character.isAlphabetic(fileContent.charAt(charNo)) || fileContent.charAt(charNo) == '_') ) {
            return null;
        }

        while (Character.isAlphabetic(fileContent.charAt(charNo)) || Character.isDigit(fileContent.charAt(charNo)) || fileContent.charAt(charNo) == '_') {
            stringBuilder.append(fileContent.charAt(charNo));
            ++charNo;
        }

        if ( !Character.isWhitespace(fileContent.charAt(charNo)) && fileContent.charAt(charNo) != '>' ) {
            return null;
        }

        //@todo potencjalnie problematyczna kwestia

        for (int i = charNo; i < fileContent.length(); i++) {
            if (fileContent.charAt(i) == '<') {
                return null;
            }
            if (fileContent.charAt(i) == '>') {
                break;
            }
        }

        fileContent = fileContent.substring(charNo);

        return new Token(TokenType.NAME, stringBuilder.toString());
    }

    private Token readXMLSTag() {
        String openingTag = "<?xml";

        if (!fileContent.startsWith(openingTag)){
            return null;
        }

        fileContent = fileContent.substring(openingTag.length());

        return new Token(TokenType.XMLSTAG, openingTag);
    }

    private Token readVersionAttribute() {
        int charNo = getFirstSymbolNo();
        String type = "version";

        if ( !(fileContent.startsWith(type, charNo) && nextLogicalSign(charNo) == '=') )  {
            return null;
        }

        fileContent = fileContent.substring(charNo + type.length());

        return new Token(TokenType.VERSION_ATTRIBUTE, type);
    }

    //private Token readAttributeValue()
    private Token readVersion() {
        int i = 0;
        if (Character.isWhitespace(fileContent.charAt(i))) {
            i = nextNonWhiteSpaceCharNo(0);
        }
        StringBuilder stringBuilder = new StringBuilder();

        if (fileContent.charAt(i) == '\'') {
            i = i + 1;
            while (i < fileContent.length()) {
                char ch = fileContent.charAt(i);
                if (ch == '\'') {
                    break;
                } else if (ch == '\"') {
                    return null;
                }
                stringBuilder.append(fileContent.charAt(i));
                i++;
            }
        } else if (fileContent.charAt(i) == '\"') {
            i = i + 1;
            while (i < fileContent.length()) {
                char ch = fileContent.charAt(i);
                if (ch == '\"') {
                    break;
                } else if (ch == '\'') {
                    return null;
                }
                stringBuilder.append(fileContent.charAt(i));
                i++;
            }
        } else {
            return null;
        }

        i++;
        fileContent = fileContent.substring(i);

        return new Token(TokenType.VERSION, stringBuilder.toString());
    }

    private Token readXMLETag() {
        String endingTag = "?>";

        if (!fileContent.startsWith(endingTag)){
            return null;
        }

        fileContent = fileContent.substring(endingTag.length());

        return new Token(TokenType.XMLETAG, endingTag);
    }

    private Token readProlog() {

        String version = "version";
        String endingTag = "?>";


        int i = nextNonWhiteSpaceCharNo(endingTag.length() - 1);

        if (!(fileContent.startsWith(version, i) &&
                nextNonWhiteSpaceChar(i + version.length() - 1) == '=')) {
            return null;
        }

        i = fileContent.indexOf('=');

        //wyrażenie nr wersji - przykladowo "1.2" (i na " lub ' po przeparsowaniu)

        if (nextNonWhiteSpaceChar(i) == '\'') {
            int j = nextNonWhiteSpaceCharNo(i);
            if (!(Character.isDigit(fileContent.charAt(j + 1)) && fileContent.charAt(j + 1) != '0' &&
                    fileContent.charAt(j + 2) == '.' &&
                    Character.isDigit(fileContent.charAt(j + 3)) &&
                    fileContent.charAt(j + 4) =='\'')) {
                return null;
            }
            i = j + 4;
        } else if (nextNonWhiteSpaceChar(i) == '\"') {
            int j = nextNonWhiteSpaceCharNo(i);
            if (!(Character.isDigit(fileContent.charAt(j + 1)) && fileContent.charAt(j + 1) != '0' &&
                    fileContent.charAt(j + 2) == '.' &&
                    Character.isDigit(fileContent.charAt(j + 3)) &&
                    fileContent.charAt(j + 4) =='\"')) {
                return null;
            }
            i = j + 4;
        } else {
            return null;
        }

        if(!fileContent.startsWith(endingTag, nextNonWhiteSpaceCharNo(i))){
            return null;
        }

        i = nextNonWhiteSpaceCharNo(i) + 2;

        String ret = fileContent.substring(0, i);

        fileContent = fileContent.substring(i);

        return new Token(TokenType.PROLOG, ret);
    }

    private Token readSTag() {
        int charNo = getFirstSymbolNo();

        if (fileContent.charAt(charNo) == '<') {
            fileContent = fileContent.substring(++charNo);
            return new Token(TokenType.STAG, "<");
        }
        return null;
    }

    private Token readETag() {
        int charNo = getFirstSymbolNo();

        if (fileContent.charAt(charNo) == '>') {
            fileContent = fileContent.substring(++charNo);
            return new Token(TokenType.ETAG, ">");
        }
        return null;
    }

    /**
     * W przypadku podania miejsca ze znakiem nie-białym zostanie wyszukana kolejna pozycja z nie-białym znakiem
     * @param start
     * @return
     */
    private int nextNonWhiteSpaceCharNo(int start) {
        if (!Character.isWhitespace(fileContent.codePointAt(start))){
            start++;
        }
        int ret = start;
        while(Character.isWhitespace(fileContent.codePointAt(ret))){
            ret++;
        }

        return ret;
    }

    /**
     * W przypadku podania miejsca ze znakiem nie-białym zostanie wyszukany kolejny nie-biały znak
     * @param start
     * @return
     */
    private char nextNonWhiteSpaceChar(int start) {
        if (!Character.isWhitespace(fileContent.codePointAt(start))){
            start++;
        }
        int ret = start;
        while(Character.isWhitespace(fileContent.codePointAt(ret))){
            ret++;
        }

        return fileContent.charAt(ret);
    }

    /**
     * Funkcja sprawdza od poczatku pole fileContent szukajac pierwszego nie-bialego znaku. Jesli już pierwszy nie jest
     * bialym znakiem to zostanie zwrocone 0.
     * @return
     */
    private int getFirstSymbolNo() {
        int charNo = 0;
        if (Character.isWhitespace(fileContent.charAt(0))){
            charNo = nextNonWhiteSpaceCharNo(0);
        }
        return charNo;
    }

    private char nextLogicalSign(int start) {
        for (int i = start; i < fileContent.length(); i++) {
            char actual = fileContent.charAt(i);

            if (actual == '<' || actual == '>' || actual == '/' || actual == '\'' || actual == '\"' || actual == '='){
                return actual;
            }
        }

        return NO_NEXT_LOGICAL_SIGN;
    }

    public Scanner(String path) {
        try {
            fileInputStream = new FileInputStream(path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("ISO-8859-2"));

            int b = inputStreamReader.read();
            StringBuilder stringBuilder = new StringBuilder();

            while(b != -1) {
                stringBuilder.append((char)b);
                b = inputStreamReader.read();
            }

            fileContent = stringBuilder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void print(){
        System.out.println(fileContent);
    }
}
