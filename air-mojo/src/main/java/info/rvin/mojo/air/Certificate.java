/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package info.rvin.mojo.air;

/**
 * Helper class to keep all certificate configuration together in one class.
 * 
 * @author Joost den Boer.
 *
 */
public class Certificate {

	private String cn; // common name
	private String ou; // organisational unit
	private String o;  // organisation
	private String c;  // country
	private String keyType; // key type
	private String pfxFile; // pfx file
	private String password; // certificate password
	
	/**
	 * Default constructor
	 */
	public Certificate() {
	}

	/**
	 * @return the cn
	 */
	public String getCn() {
		return cn;
	}

	/**
	 * @param cn the cn to set
	 */
	public void setCn(String cn) {
		this.cn = cn;
	}

	/**
	 * @return <code>true</code> if cn attribute is set
	 */
	public boolean hasCn() {
		return hasAttibute(cn);
	}

	/**
	 * @return the ou
	 */
	public String getOu() {
		return ou;
	}

	/**
	 * @param ou the ou to set
	 */
	public void setOu(String ou) {
		this.ou = ou;
	}

	/**
	 * @return <code>true</code> if ou attribute is set
	 */
	public boolean hasOu() {
		return hasAttibute(ou);
	}

	/**
	 * @return the o
	 */
	public String getO() {
		return o;
	}

	/**
	 * @param o the o to set
	 */
	public void setO(String o) {
		this.o = o;
	}

	/**
	 * @return <code>true</code> if o attribute is set
	 */
	public boolean hasO() {
		return hasAttibute(o);
	}

	/**
	 * @return the c
	 */
	public String getC() {
		return c;
	}

	/**
	 * @param c the c to set
	 */
	public void setC(String c) {
		this.c = c;
	}

	/**
	 * @return <code>true</code> if c attribute is set
	 */
	public boolean hasC() {
		return hasAttibute(c);
	}

	/**
	 * @return the keyType
	 */
	public String getKeyType() {
		return keyType;
	}

	/**
	 * @param keyType the keyType to set
	 */
	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	/**
	 * @return <code>true</code> if keyType attribute is set
	 */
	public boolean hasKeyType() {
		return hasAttibute(keyType);
	}

	/**
	 * @return the pfxFile
	 */
	public String getPfxFile() {
		return pfxFile;
	}

	/**
	 * @param pfxFile the pfxFile to set
	 */
	public void setPfxFile(String pfxFile) {
		this.pfxFile = pfxFile;
	}

	/**
	 * @return <code>true</code> if pfxFile attribute is set
	 */
	public boolean hasPfxFile() {
		return hasAttibute(pfxFile);
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return <code>true</code> if password attribute is set
	 */
	public boolean hasPassword() {
		return hasAttibute(password);
	}
	
	/**
	 * @param attrValue String value of attribute to check
	 * @return <code>true</code> if given attribute value is set
	 */
	private boolean hasAttibute(String attrValue) {
		return null != attrValue && !"".equals(attrValue);
	}

}