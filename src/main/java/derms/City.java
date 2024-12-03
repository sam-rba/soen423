package derms;

import java.io.Serializable;

public class City implements Serializable {
	public static final int codeLen = 3;

	private String code;

	public City(String code) throws IllegalArgumentException {
		if (code.length() != codeLen)
			throw new IllegalArgumentException("Invalid city: "+code+"; must be "+codeLen+" letters");
		this.code = code;
	}

	public City() {
		this("XXX");
	}

	public String getCode() { return code; }

	@Override
	public String toString() {
		return code;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		City other = (City) obj;
		return this.code.equals(other.code);
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}
}
