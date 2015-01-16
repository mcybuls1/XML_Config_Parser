package pl.mcybulski.XMLConfig;

import pl.mcybulski.XMLConfig.commons.TYPES;
import pl.mcybulski.XMLConfig.commons.Token;
import pl.mcybulski.XMLConfig.commons.TokenType;
import pl.mcybulski.XMLConfig.exceptions.ImproperGrammaticalException;
import pl.mcybulski.XMLConfig.exceptions.UnrecognizedTokenException;
import pl.mcybulski.XMLConfig.exceptions.ValueParsingException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @TODO Obuduj dobrze wyjątkami!!
 * Created by Mikołaj on 2014-12-22.
 */
public class Parser {

    public final static String KOMUNIKAT = "Wystąpił bliżej nieokreślony błąd";
    private Scanner scanner;

    public Parser(String path) {
        scanner = new Scanner(path);
    }

    /**
     *
     * @return
     * @throws ImproperGrammaticalException
     * @throws UnrecognizedTokenException
     */
    public Configuration parseXML() throws Exception {

        Configuration config = new Configuration();
        if (!parseProlog());
        if ( !scanner.getNextToken().getTokenType().equals(TokenType.PROLOG ) ) {
            throw new ImproperGrammaticalException("Brak prologa");
        }

        if ( !scanner.getNextToken().getTokenType().equals(TokenType.SCONFIG) ) {
            throw new ImproperGrammaticalException("Brak startowego tagu <Config>");
        }

        Token nextToken = scanner.getNextToken();
    bool parseConfigOpen()
        {

            if (token==STAG)
            {
                getNextToken();
                if (token==NAME && token.value=="Config")
                {

                    getNextToken();
                    if token==ETAG)
                        return true;
                }
            }

            return false
        }
        while ( !nextToken.getTokenType().equals(TokenType.ECONFIG) ) {

            if ( nextToken.getTokenType().equals(TokenType.STAG) ) {

                Token nameToken = scanner.getNextToken();

                if ( nameToken.getTokenType().equals(TokenType.NAME) ) {

                    nextToken = scanner.getNextToken();

                    if ( nextToken.getTokenType().equals(TokenType.TYPE_ATTRIBUTE) ) {
                        Parameter newParameter = readTypedTags(nameToken);
                        config.addParameter(newParameter.getKey(), newParameter.getValue());

                    } else if ( nextToken.getTokenType().equals(TokenType.ARRAY_ATTRIBUTE) ) {
                        Parameter newParameter = readArrayTags(nameToken);
                        config.addParameter(newParameter.getKey(), newParameter.getValue());

                    } else if ( nextToken.getTokenType().equals(TokenType.ETAG) ) {
                        Configuration configuration = readConfigTags(nameToken);
                        config.addParameter(nameToken.getValue(), configuration);

                    } else {
                        throw new ImproperGrammaticalException("Nieprawidłowo określony obiekt - po <" + nameToken.getValue()
                                + " mogą wystąpić tylko 'type', 'array' lub '>'");
                    }

                } else {
                    throw new ImproperGrammaticalException("Brak nazwy po znaku <");
                }
            }
            nextToken = scanner.getNextToken();
        }


        return config;
    }

    private Configuration readConfigTags(Token name) throws Exception {
        try {
            Configuration config = new Configuration();

            Token nextToken = scanner.getNextToken();

            while (true) {

                if (nextToken.getTokenType().equals(TokenType.STAG)) {

                    Token nameToken = scanner.getNextToken();

                    if (nameToken.getTokenType().equals(TokenType.NAME)) {

                        nextToken = scanner.getNextToken();

                        if (nextToken.getTokenType().equals(TokenType.TYPE_ATTRIBUTE)) {
                            Parameter newParameter = readTypedTags(nameToken);
                            config.addParameter(newParameter.getKey(), newParameter.getValue());

                        } else if (nextToken.getTokenType().equals(TokenType.ARRAY_ATTRIBUTE)) {
                            Parameter newParameter = readArrayTags(nameToken);
                            config.addParameter(newParameter.getKey(), newParameter.getValue());

                        } else if (nextToken.getTokenType().equals(TokenType.ETAG)) {
                            Configuration configuration = readConfigTags(nameToken);
                            config.addParameter(nameToken.getValue(), configuration);

                        } else {
                            throw new ImproperGrammaticalException("Nieprawidłowo określony obiekt - po <" + nameToken.getValue()
                                    + " mogą wystąpić tylko 'type', 'array' lub '>'");
                        }

                    } else if (nameToken.getTokenType().equals(TokenType.SLASH)) {
                        nameToken = scanner.getNextToken();

                        if (nameToken.equals(name) && scanner.getNextToken().getTokenType().equals(TokenType.ETAG)) {
                            return config; //zakonczone na </name> -->
                        } else {
                            throw new ImproperGrammaticalException("Brak nazwy w końcowym tagu, niezgodna nazwa względem tagu początkowego lub brak '>' po nazwie");
                        }
                    }

                } else {
                    throw new ImproperGrammaticalException("Nie znaleziono rozpoczęcia kolejnego taga, a dokument nie jest jeszcze zakończony");
                }
                nextToken = scanner.getNextToken();
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
            Token nextToken = scanner.getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.EQUALS)) {
                throw new ImproperGrammaticalException("Brak znaku '=' po type");
            }

            Token typeToken = scanner.getNextToken();

            if (!typeToken.getTokenType().equals(TokenType.TYPE)) {
                throw new ImproperGrammaticalException("Brak typu po znaku '='");
            }

            nextToken = scanner.getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.ETAG)) {
                throw new ImproperGrammaticalException("Brak domkniecia poczatkowego taga '>'");
            }

            Token valueToken = scanner.getNextToken();

            if (!valueToken.getTokenType().equals(TokenType.VALUE) && !typeToken.getValue().equals(TYPES.NULL)) {
                throw new ImproperGrammaticalException("Brak wartosci elementu po początkowym tagu");
            }

            Object value = parseValue(typeToken, valueToken);

            if (typeToken.getValue().equals(TYPES.NULL)) {
                if (!valueToken.getTokenType().equals(TokenType.STAG)) {
                    throw new ImproperGrammaticalException("Nieprawidłowa struktura obiektu typu null - nie wykryto '<' po znaku '>'");
                }
            } else {
                nextToken = scanner.getNextToken();

                if (!nextToken.getTokenType().equals(TokenType.STAG)) {
                    throw new ImproperGrammaticalException("Brak otwarcia końcowego taga '<'");
                }
            }

            nextToken = scanner.getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.SLASH)) {
                throw new ImproperGrammaticalException("Brak '/' po znaku '<' w końcowym tagu");
            }

            Token nameToken = scanner.getNextToken();

            if (!nameToken.getTokenType().equals(TokenType.NAME)) {
                throw new ImproperGrammaticalException("Brak nazwy elementu w końcowym tagu po znakach '</'");
            }

            if (!nameToken.equals(name)) {
                throw new ImproperGrammaticalException("Niezgodna nazwa elementu w końcowym tagu względem początkowego");
            }

            nextToken = scanner.getNextToken();

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
            Token nextToken = scanner.getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.EQUALS)) {
                throw new ImproperGrammaticalException("Brak rowna sie");
            }

            Token typeToken = scanner.getNextToken();

            if (!typeToken.getTokenType().equals(TokenType.TYPE)) {
                throw new ImproperGrammaticalException("Brak typu po rowna sie");
            }

            if ( typeToken.getValue().trim().toLowerCase().equals("null") ) {
                throw new ImproperGrammaticalException("Typ null niedozwolony dla wartości w tablicy");
            }

            nextToken = scanner.getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.ETAG)) {
                throw new ImproperGrammaticalException("Brak domkniecia poczatkowego taga");
            }

            //Startuja wartosci

            nextToken = scanner.getNextToken();

            if (!nextToken.getTokenType().equals(TokenType.STAG)) {
                throw new ImproperGrammaticalException("Nieoczekiwany token po deklaracji tablicy");
            }

            nextToken = scanner.getNextToken();

            List<Object> values = new ArrayList<>();

            while ( !nextToken.getTokenType().equals(TokenType.SLASH) ) {
                Object element = readArrayElement(nextToken, typeToken);
                values.add(element);

                nextToken = scanner.getNextToken();

                if ( !nextToken.getTokenType().equals(TokenType.STAG) ) {
                    throw new ImproperGrammaticalException("Niespodziewany token po odczytaniu elementu tablicy");
                }

                nextToken = scanner.getNextToken();
            }

            //Koncowy tag jest </ -->

            Token nameToken = scanner.getNextToken();

            if (!nameToken.getTokenType().equals(TokenType.NAME)) {
                throw new ImproperGrammaticalException("Brak nazwy elementu w końcowym tagu");
            }

            if (!nameToken.equals(name)) {
                throw new ImproperGrammaticalException("Niezgodna nazwa elementu w końcowym tagu tablicy względem początkowego");
            }

            nextToken = scanner.getNextToken();

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

        nextToken = scanner.getNextToken();

        if ( !nextToken.getTokenType().equals(TokenType.ETAG) ) {
            throw new ImproperGrammaticalException("Brak zamknięcia początkowego taga elementu tablicy");
        }

        Token valueToken = scanner.getNextToken();

        if ( !valueToken.getTokenType().equals(TokenType.VALUE) ) {
            throw new ImproperGrammaticalException("Brak wartości w elemencie tablicy");
        }

        Object value = parseValue(typeToken, valueToken);

        nextToken = scanner.getNextToken();

        if ( !nextToken.getTokenType().equals(TokenType.STAG) ) {
            throw new ImproperGrammaticalException("Brak otwarcia końcowego taga elementu tablicy");
        }

        nextToken = scanner.getNextToken();

        if ( !nextToken.getTokenType().equals(TokenType.SLASH) ) {
            throw new ImproperGrammaticalException("Brak slasha końcowego taga elementu tablicy");
        }

        nextToken = scanner.getNextToken();

        if ( !nextToken.equals(new Token(TokenType.NAME, "value")) ) {
            throw new ImproperGrammaticalException("Brak value w nazwie końcowego taga elementu tablicy");
        }

        nextToken = scanner.getNextToken();

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
            char ch = value.charAt(i);
            if (i == 0) {
                if (new BigDecimal(0).equals(recognizeDigit(ch))) {
                    if (value.length() != 1) {
                        if (value.charAt(i + 1) != '.') {
                            throw new ValueParsingException("Po cyfrze 0 może wystąpić tylko '.' i dalsze cyfry");
                        }
                    }

                } else {
                    if (recognizeDigit(ch) == null) {
                        throw new ValueParsingException("Nie rozpoznano cyfry na pierwszym miejscu wartośći");
                    } else {
                        bigDecimal = recognizeDigit(ch);
                    }
                }

            } else {
                if (recognizeDigit(ch) != null) {
                    bigDecimal = bigDecimal.multiply(new BigDecimal(10));
                    bigDecimal = bigDecimal.add(recognizeDigit(ch));
                } else if (ch == '.' && !commaFound) {
                    digitsAfterComma = value.length() - i - 1;
                    commaFound = true;
                } else if (ch == '.') {
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