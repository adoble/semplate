package semplate;

import java.net.URL;
import java.time.*;

import semplate.annotations.Templatable;
import semplate.annotations.TemplateField;

@Templatable
public class AllTypes {
	 @TemplateField
	 private String str;
	 
	 @TemplateField
	 private Integer intWrapper;  

	 @TemplateField
	 private int intPrimitive;

	 @TemplateField
	 private Short shortWrapper;  

	 @TemplateField
	 private short shortPrimitive;

	 @TemplateField
	 private Byte byteWrapper;  

	 @TemplateField
	 private byte bytePrimitive;

	 @TemplateField
	 private Long longWrapper;  

	 @TemplateField
	 private long longPrimitive;

	 @TemplateField
	 private Double doubleWrapper;  

	 @TemplateField
	 private double doublePrimitive;

	 @TemplateField
	 private Float floatWrapper;  

	 @TemplateField
	 private float floatPrimitive;

	 @TemplateField
	 private Boolean booleanWrapper;  

	 @TemplateField
	 private boolean booleanPrimitive;

	 @TemplateField
	 private Character characterWrapper;  

	 @TemplateField
	 private char characterPrimitive;

	 @TemplateField
	 private LocalDate localDate;

	 @TemplateField
	 private LocalDateTime localDateTime;

	 @TemplateField
	 private ZonedDateTime zonedDateTime;

	 @TemplateField
	 private URL url;
	 
	 
	 
	 public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}
	public Integer getIntWrapper() {
		return intWrapper;
	}
	public void setIntWrapper(Integer intWrapper) {
		this.intWrapper = intWrapper;
	}
	public int getIntPrimitive() {
		return intPrimitive;
	}
	public void setIntPrimitive(int intPrimitive) {
		this.intPrimitive = intPrimitive;
	}
	public Short getShortWrapper() {
		return shortWrapper;
	}
	public void setShortWrapper(Short shortWrapper) {
		this.shortWrapper = shortWrapper;
	}
	public short getShortPrimitive() {
		return shortPrimitive;
	}
	public void setShortPrimitive(short shortPrimitive) {
		this.shortPrimitive = shortPrimitive;
	}
	public Byte getByteWrapper() {
		return byteWrapper;
	}
	public void setByteWrapper(Byte byteWrapper) {
		this.byteWrapper = byteWrapper;
	}
	public byte getBytePrimitive() {
		return bytePrimitive;
	}
	public void setBytePrimitive(byte bytePrimitive) {
		this.bytePrimitive = bytePrimitive;
	}
	public Long getLongWrapper() {
		return longWrapper;
	}
	public void setLongWrapper(Long longWrapper) {
		this.longWrapper = longWrapper;
	}
	public long getLongPrimitive() {
		return longPrimitive;
	}
	public void setLongPrimitive(long longPrimitive) {
		this.longPrimitive = longPrimitive;
	}
	public Double getDoubleWrapper() {
		return doubleWrapper;
	}
	public void setDoubleWrapper(Double doubleWrapper) {
		this.doubleWrapper = doubleWrapper;
	}
	public double getDoublePrimitive() {
		return doublePrimitive;
	}
	public void setDoublePrimitive(double doublePrimitive) {
		this.doublePrimitive = doublePrimitive;
	}
	public Float getFloatWrapper() {
		return floatWrapper;
	}
	public void setFloatWrapper(Float floatWrapper) {
		this.floatWrapper = floatWrapper;
	}
	public float getFloatPrimitive() {
		return floatPrimitive;
	}
	public void setFloatPrimitive(float floatPrimitive) {
		this.floatPrimitive = floatPrimitive;
	}
	public Boolean getBooleanWrapper() {
		return booleanWrapper;
	}
	public void setBooleanWrapper(Boolean booleanWrapper) {
		this.booleanWrapper = booleanWrapper;
	}
	public boolean getBooleanPrimitive() {
		return booleanPrimitive;
	}
	public void setBooleanPrimitive(boolean booleanPrimitive) {
		this.booleanPrimitive = booleanPrimitive;
	}
	public Character getCharacterWrapper() {
		return characterWrapper;
	}
	public void setCharacterWrapper(Character characterWrapper) {
		this.characterWrapper = characterWrapper;
	}
	public char getCharacterPrimitive() {
		return characterPrimitive;
	}
	public void setCharacterPrimitive(char characterPrimitive) {
		this.characterPrimitive = characterPrimitive;
	}
	public LocalDate getLocalDate() {
		return localDate;
	}
	public void setLocalDate(LocalDate localDate) {
		this.localDate = localDate;
	}
	public LocalDateTime getLocalDateTime() {
		return localDateTime;
	}
	public void setLocalDateTime(LocalDateTime localDateTime) {
		this.localDateTime = localDateTime;
	}
	public ZonedDateTime getZonedDateTime() {
		return zonedDateTime;
	}
	public void setZonedDateTime(ZonedDateTime zonedDateTime) {
		this.zonedDateTime = zonedDateTime;
	}
	public URL getURL() {
		return url;
	}
	public void setURL(URL url) {
		this.url = url;
	}
	
}
