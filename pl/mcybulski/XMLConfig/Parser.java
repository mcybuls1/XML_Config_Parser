package pl.mcybulski.XMLConfig;

import pl.mcybulski.XMLConfig.commons.TYPES;
import pl.mcybulski.XMLConfig.commons.Token;
import pl.mcybulski.XMLConfig.commons.TokenType;
import pl.mcybulski.XMLConfig.exceptions.ImproperGrammaticalException;
import pl.mcybulski.XMLConfig.exceptions.UnrecognizedTokenException;
import pl.mcybulski.XMLConfig.exceptions.ValueParsingException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * Created by Mikołaj on 2014-12-22.
 */
public class Parser {

    private Scanner scanner;
    private Queue<Token> tokenBuffer;

    public Parser(String path) {
        scanner = new Scanner(path);
        tokenBuffer = new LinkedList<>();
    }

    /**
     *
     * @return
     * @throws ImproperGrammaticalException
     * @throws UnrecognizedTokenException
     */
    public Configuration parseXML() throws Exception {

        Configuration config;

        parseProlog();

        Token nameToken = readNamePartFromTag();

        if ( nameToken == null ) {
            throw new ImproperGrammaticalException("Brak wymaganego elementu");
        } else if ( !nameToken.getValue().equals("Config") ) {
            throw new ImproperGrammaticalException("Pierwszy element ma nazwę różną od 'Config'");
        }

        config = (Configuration)readConfigTags(nameToken).getValue();

        if (!getNextToken().getTokenType().equals(TokenType.EOF)) {
            throw new ImproperGrammaticalException("Smieci na końcu pliku");
        }

        return config;
    }

    private void parseProlog() throws UnrecognizedTokenException, ImproperGrammaticalException {

        if ( !getNextToken().getTokenType().equals(TokenType.XMLSTAG) ) {
            throw new ImproperGrammaticalException("Brak rozpoczęcia taga prologa '<?xml'");
        }

        if ( !getNextToken().getTokenType().equals(TokenType.VERSION_ATTRIBUTE) ) {
            throw new ImproperGrammaticalException("Brak atrybutu version w prologu");
        }

        if ( !getNextToken().getTokenType().equals(TokenType.EQUALS) ) {
            throw new ImproperGrammaticalException("Brak znaku '=' po atrybucie version w proogu");
        }

        Token version = getNextToken();

        if ( !version.getTokenType().equals(TokenType.VERSION) ) {
            throw new ImproperGrammaticalException("Brak wartości wersji w atrybucie version w prologu");
        }

        String versionValue = version.getValue();

        if ( !"1.0".equals(versionValue) && !"1.1".equals(versionValue) ) {
            throw new ImproperGrammaticalException("Niepoprawna wersja xml dokumentu - wartości poprawne to 1.0 lub 1.1");
        }

        if ( !getNextToken().getTokenType().equals(TokenType.XMLETAG) ) {
            throw new ImproperGrammaticalException("Brak poprawnego zakończenia taga prologu");
        }
    }

    //@TODO Moze być kłopot
    private Token readNamePartFromTag() throws UnrecognizedTokenException, ImproperGrammaticalException {
        Token nextToken = getNextToken();

        if ( !nextToken.getTokenType().equals(TokenType.STAG) ) {
            tokenBuffer.add(nextToken);
            return null;
        }

        Token nameToken = getNextToken();

        if ( !nameToken.getTokenType().equals(TokenType.NAME) && !nameToken.getTokenType().equals(TokenType.SLASH)) {
            throw new ImproperGrammaticalException("Brak nazwy po rozpoczęciu taga");
        } else if (nameToken.getTokenType().equals(TokenType.SLASH)) {
            tokenBuffer.add(nameToken);
            return null;
        } else {
            return nameToken;
        }
    }

    private Token getNextToken() throws UnrecognizedTokenException {
        Token ret = tokenBuffer.poll();
        if (ret != null) {
            return ret;
        } else {
            return scanner.getNextToken();
        }
    }

    /**
     *
     * @param name
     * @return
     * @throws Exception
     */
    private Parameter readConfigTags(Token name) throws Exception {
        try {
            Configuration config = new Configuration();

            Token nextToken = getNextToken();

            if ( !nextToken.getTokenType().equals(TokenType.ETAG) ) {
                tokenBuffer.add(nextToken);
                return null;
            }

            while (true) {
                Token parameterNameToken = readNamePartFromTag();
                if (parameterNameToken == null) {
                    break;
                }

                Parameter parameter;

                parameter = readTypedTags(parameterNameToken);

                if (parameter != null) {
                    config.addParameter(parameter.getKey(), parameter.getValue());
                    continue;
                }

                parameter = readArrayTags(parameterNameToken);

                if (parameter != null) {
                    config.addParameter(parameter.getKey(), parameter.getValue());
                    continue;
                }

                parameter = readConfigTags(parameterNameToken);

                if (parameter != null) {
                    config.addParameter(parameter.getKey(), parameter.getValue());
                    continue;
                }

                throw new ImproperGrammaticalException("Nieprawidłowo określony obiekt - po <" + parameterNameToken.getValue()
                        + " mogą wystąpić tylko 'type', 'array' lub '>'");
            }

            nextToken = getNextToken();

            if ( !nextToken.getTokenType().equals(TokenType.SLASH)) {
                throw new ImproperGrammaticalException("Niepoprawny tag końcowy - brak '/'");
            }

            Token nameToken = getNextToken();

            if (nameToken.equals(name) && getNextToken().getTokenType().equals(TokenType.ETAG)) {
                return new Parameter(name.getValue(), config); //zakonczone na </name> -->
            } else {
                throw new ImproperGrammaticalException("Brak nazwy w końcowym tagu, niezgodna nazwa względem tagu początkowego lub brak '>' po nazwie");
            }

        } catch (Exception e) {
            throw new Exception(name.getValue() + ": " + e.getMessage(), e);
        }
    }

    /*
    Token{tokenType=STAG, value='<'}
    Token{tokenType=NAME, value='SettingsFile'}
    Token{tokenType=TYPE_ATTRIBUTE, value='type'}
    Token{tokenType=EQUALS, value='='}
    Token{tokenType=TYPE, value='string'}
    Token{tokenType=ETAG, value='>'}
    Token{tokenType=VALUE, value='C:/Windows/conf.cfg'}
    Token{tokenType=STAG, value='<'}
    Token{tokenType=SLASH, value='/'}
    Token{tokenType=NAME, value='SettingsFile'}
    Token{tokenType=ETAG, value='>'}
     */
    private Parameter readTypedTags(Token name) throws Exception {

        try {
            Token nextToken = getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.TYPE_ATTRIBUTE)) {
                tokenBuffer.add(nextToken);
                return null;
            }

            nextToken = getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.EQUALS)) {
                throw new ImproperGrammaticalException("Brak znaku '=' po type");
            }

            Token typeToken = getNextToken();

            if (!typeToken.getTokenType().equals(TokenType.TYPE)) {
                throw new ImproperGrammaticalException("Brak typu po znaku '='");
            }

            nextToken = getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.ETAG)) {
                throw new ImproperGrammaticalException("Brak domkniecia poczatkowego taga '>'");
            }

            Token valueToken = getNextToken();

            if (!valueToken.getTokenType().equals(TokenType.VALUE) && !typeToken.getValue().equals(TYPES.NULL)) {
                throw new ImproperGrammaticalException("Brak wartosci elementu po początkowym tagu");
            }

            Object value = parseValue(typeToken, valueToken);

            if (typeToken.getValue().equals(TYPES.NULL)) {
                if (!valueToken.getTokenType().equals(TokenType.STAG)) {
                    throw new ImproperGrammaticalException("Nieprawidłowa struktura obiektu typu null - nie wykryto '<' po znaku '>'");
                }
            } else {
                nextToken = getNextToken();

                if (!nextToken.getTokenType().equals(TokenType.STAG)) {
                    throw new ImproperGrammaticalException("Brak otwarcia końcowego taga '<'");
                }
            }

            nextToken = getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.SLASH)) {
                throw new ImproperGrammaticalException("Brak '/' po znaku '<' w końcowym tagu");
            }

            Token nameToken = getNextToken();

            if (!nameToken.getTokenType().equals(TokenType.NAME)) {
                throw new ImproperGrammaticalException("Brak nazwy elementu w końcowym tagu po znakach '</'");
            }

            if (!nameToken.equals(name)) {
                throw new ImproperGrammaticalException("Niezgodna nazwa elementu w końcowym tagu względem początkowego");
            }

            nextToken = getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.ETAG)) {
                throw new ImproperGrammaticalException("Brak zakończenia końcowego taga '>'");
            }

            return new Parameter(name.getValue(), value);

        } catch (Exception e) {
            throw new Exception(name.getValue() + ": " + e.getMessage(), e);
        }
    }

    private Parameter readArrayTags(Token name) throws Exception {
        try {
            Token nextToken = getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.ARRAY_ATTRIBUTE)) {
                tokenBuffer.add(nextToken);
                return null;
            }

            nextToken = getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.EQUALS)) {
                throw new ImproperGrammaticalException("Brak rowna sie");
            }

            Token typeToken = getNextToken();

            if (!typeToken.getTokenType().equals(TokenType.TYPE)) {
                throw new ImproperGrammaticalException("Brak typu po rowna sie");
            }

            if ( typeToken.getValue().trim().toLowerCase().equals("null") ) {
                throw new ImproperGrammaticalException("Typ null niedozwolony dla wartości w tablicy");
            }

            nextToken = getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.ETAG)) {
                throw new ImproperGrammaticalException("Brak domkniecia poczatkowego taga");
            }

            //Startuja wartosci

            nextToken = getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.STAG)) {
                throw new ImproperGrammaticalException("Nieoczekiwany token po deklaracji tablicy");
            }

            nextToken = getNextToken();

            List<Object> values = new ArrayList<>();

            while ( !nextToken.getTokenType().equals(TokenType.SLASH) ) {
                Object element = readArrayElement(nextToken, typeToken);
                values.add(element);

                nextToken = getNextToken();

                if ( !nextToken.getTokenType().equals(TokenType.STAG) ) {
                    throw new ImproperGrammaticalException("Niespodziewany token po odczytaniu elementu tablicy");
                }

                nextToken = getNextToken();
            }

            //Koncowy tag jest </ -->

            Token nameToken = getNextToken();

            if (!nameToken.getTokenType().equals(TokenType.NAME)) {
                throw new ImproperGrammaticalException("Brak nazwy elementu w końcowym tagu");
            }

            if (!nameToken.equals(name)) {
                throw new ImproperGrammaticalException("Niezgodna nazwa elementu w końcowym tagu tablicy względem początkowego");
            }

            nextToken = getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.ETAG)) {
                throw new ImproperGrammaticalException("Brak zakończenia końcowego taga tablicy");
            }

            return new Parameter(name.getValue(), values);

        } catch (Exception e) {
            throw new Exception(name.getValue() + ": " + e.getMessage(), e);
        }
    }

    private Object readArrayElement(Token nameToken, Token typeToken) throws ImproperGrammaticalException, UnrecognizedTokenException, ValueParsingException {

        Token nextToken = nameToken;

        if ( !nextToken.equals(new Token(TokenType.NAME, "value")) ) {
            throw new ImproperGrammaticalException("Brak value w nazwie początkowego taga elementu tablicy");
        }

        nextToken = getNextToken();

        if ( !nextToken.getTokenType().equals(TokenType.ETAG) ) {
            throw new ImproperGrammaticalException("Brak zamknięcia początkowego taga elementu tablicy");
        }

        Token valueToken = getNextToken();

        if ( !valueToken.getTokenType().equals(TokenType.VALUE) ) {
            throw new ImproperGrammaticalException("Brak wartości w elemencie tablicy");
        }

        Object value = parseValue(typeToken, valueToken);

        nextToken = getNextToken();

        if ( !nextToken.getTokenType().equals(TokenType.STAG) ) {
            throw new ImproperGrammaticalException("Brak otwarcia końcowego taga elementu tablicy");
        }

        nextToken = getNextToken();

        if ( !nextToken.getTokenType().equals(TokenType.SLASH) ) {
            throw new ImproperGrammaticalException("Brak slasha końcowego taga elementu tablicy");
        }

        nextToken = getNextToken();

        if ( !nextToken.equals(new Token(TokenType.NAME, "value")) ) {
            throw new ImproperGrammaticalException("Brak value w nazwie końcowego taga elementu tablicy");
        }

        nextToken = getNextToken();

        if ( !nextToken.getTokenType().equals(TokenType.ETAG) ) {
            throw new ImproperGrammaticalException("Brak value w nazwie końcowego taga elementu tablicy");
        }

        return value;
    }

    private Object parseValue(Token typeToken, Token valueToken) throws ValueParsingException, ImproperGrammaticalException {
        Object value;

        switch ( typeToken.getValue() ) {
            case TYPES.NULL:
                value = null;
                break;

            case TYPES.BOOLEAN:
                value = parseBoolean(valueToken.getValue());
                break;

            case TYPES.DOUBLE:
                value = parseDouble(valueToken.getValue());
                break;

            case TYPES.FLOAT:
                value = parseFloat(valueToken.getValue());
                break;

            case TYPES.INTEGER:
                value = parseInteger(valueToken.getValue());
                break;

            case TYPES.LONG:
                value = parseLong(valueToken.getValue());
                break;

            case TYPES.STRING:
                value = valueToken.getValue();
                break;

            default:
                throw new ImproperGrammaticalException("Nie rozpoznano typu obiektu");
        }
        return value;
    }

    private boolean parseBoolean(String value) throws ValueParsingException {
        value = value.trim().toLowerCase();

        if ( "true".equals(value) ) {
            return true;
        } else if ( "false".equals(value) ) {
            return false;
        } else {
            throw new ValueParsingException("Brak spodziewanej wartości dla typu boolean");
        }
    }

    private BigDecimal getBigDecimal(String value) throws ValueParsingException {
        BigDecimal bigDecimal = new BigDecimal(0);
        BigDecimal plusOrMinus;
        if (value.charAt(0) == '-') {
            plusOrMinus = new BigDecimal(-1);
            value = value.substring(1);
        } else {
            plusOrMinus = new BigDecimal(1);
        }

        int digitsAfterComma = 0;
        boolean commaFound = false;

        for (int i = 0; i < value.length(); i++) {
            char charAt = value.charAt(i);
            if (i == 0) {
                if (new BigDecimal(0).equals(recognizeDigit(charAt))) {
                    if (value.length() != 1) {
                        if (value.charAt(i + 1) != '.') {
                            throw new ValueParsingException("Po cyfrze 0 może wystąpić tylko '.' i dalsze cyfry");
                        }
                    }

                } else {
                    if (recognizeDigit(charAt) == null) {
                        throw new ValueParsingException("Nie rozpoznano cyfry na pierwszym miejscu wartośći");
                    } else {
                        bigDecimal = recognizeDigit(charAt);
                    }
                }

            } else {
                if (recognizeDigit(charAt) != null) {
                    bigDecimal = bigDecimal.multiply(new BigDecimal(10));
                    bigDecimal = bigDecimal.add(recognizeDigit(charAt));
                } else if (charAt == '.' && !commaFound) {
                    digitsAfterComma = value.length() - i - 1;
                    commaFound = true;
                } else if (charAt == '.') {
                    throw new ValueParsingException("W wartości występuje więcej niż jedna kropka");
                } else {
                    throw new ValueParsingException("Nie rozpoznano cyfry, ani znaku '.'");
                }
            }
        }

        if (digitsAfterComma > 0) {
            bigDecimal = bigDecimal.divide(new BigDecimal(10).pow(digitsAfterComma));
        }

        bigDecimal = bigDecimal.multiply(plusOrMinus);
        return bigDecimal;
    }

    private double parseDouble(String value) throws ValueParsingException {
        BigDecimal bigDecimal = getBigDecimal(value);
        return bigDecimal.doubleValue();
    }

    private float parseFloat(String value) throws ValueParsingException {
        BigDecimal bigDecimal = getBigDecimal(value);
        return bigDecimal.floatValue();
    }

    private int parseInteger(String value) throws ValueParsingException {
        BigDecimal bigDecimal = getBigDecimal(value);
        try {
            int ret = bigDecimal.intValueExact();
            return ret;
        } catch (ArithmeticException e) {
            throw new ValueParsingException("Zadeklarowany typ 'int' zawiera część ułamkową, bądź przekracza zakres");
        }
    }

    private long parseLong(String value) throws ValueParsingException {
        BigDecimal bigDecimal = getBigDecimal(value);
        try {
            long ret = bigDecimal.longValueExact();
            return ret;
        } catch (ArithmeticException e) {
            throw new ValueParsingException("Zadeklarowany typ 'long' zawiera część ułamkową, bądź przekracza zakres");
        }
    }

    private BigDecimal recognizeDigit(char ch) {
        switch (ch) {
            case '0':
                return new BigDecimal(0);
            case '1':
                return new BigDecimal(1);
            case '2':
                return new BigDecimal(2);
            case '3':
                return new BigDecimal(3);
            case '4':
                return new BigDecimal(4);
            case '5':
                return new BigDecimal(5);
            case '6':
                return new BigDecimal(6);
            case '7':
                return new BigDecimal(7);
            case '8':
                return new BigDecimal(8);
            case '9':
                return new BigDecimal(9);
            default:
                return null;
        }
    }
}



class Parameter {

    private String key;
    private Object value;

    public Parameter(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}