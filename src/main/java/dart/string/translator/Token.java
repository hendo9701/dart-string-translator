package dart.string.translator;

import java.util.Objects;

public final class Token {
    final String id;
    final String plainText;

    public Token(String id, String plainText) {
        this.id = id;
        this.plainText = plainText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return id.equals(token.id) &&
                plainText.equals(token.plainText);
    }

    @Override
    public String toString() {
        return "Token{" +
                "id='" + id + '\'' +
                ", enText='" + plainText + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, plainText);

    }
}
