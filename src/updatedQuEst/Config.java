/***********************************************************************
UpdatedQuEst - online version of the QuEst software
Copyright (C) 2013 Matecat (ICT-2011.4.2-287688).

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
***********************************************************************/

package updatedQuEst;


import java.io.FileInputStream;
import java.util.Properties;
 
public class Config
{
   Properties configFile;
   public Config(String file)
   {
    configFile = new java.util.Properties();
    try {
    	//System.out.println(file);
    //  configFile.load(this.getClass().getClassLoader().getResourceAsStream(file));
    	 configFile.load(new FileInputStream(file));
    }catch(Exception eta){
        eta.printStackTrace();
    }
   }
 
   public String getProperty(String key)
   {
    String value = this.configFile.getProperty(key);
    return value;
   }
}