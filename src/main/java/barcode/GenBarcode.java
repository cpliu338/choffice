package barcode;

import java.net.*;
import java.io.*;
import java.util.regex.*;
import barcode.jbarcodebean.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenBarcode {

	File base;
	String filename="";
	String code = "";
	String codetype = "";
	int barcodeHeight;
	int narrowestBarWidth;
	boolean showtext;
	static final File defaultbase;

	static {
		defaultbase = new File(System.getProperty("java.io.tmpdir"));
	}
	
  public GenBarcode(){
        showtext=true;
        narrowestBarWidth = 1;
        barcodeHeight = 30;
        base = defaultbase;
  }
 
    public void setShowtext(boolean show){
	this.showtext=show;
    }

    public void setBasePath(String desc) {
	  File temp = new File(desc);
	  if (temp.isDirectory() && temp.canWrite()) {
		  base = temp;
	  }
	  else {
		  base = defaultbase;
		  throw new IllegalArgumentException("Cannot write to directory "+desc);
	  }
         System.out.println(base.getAbsolutePath());
    }

	public void setCode(int id){
            if (id < 0 || id >10000)
                throw new IllegalArgumentException("Illegal id (must be 1-1000: "+id);
            String code2 = String.format("%d", id*107+10000000);
   	 Pattern p = Pattern.compile("(\\d)\\1");
   	 Matcher m = p.matcher(code2);
         code = m.replaceAll("$1A");
         this.filename = code+".gif";
	}

	public String getFileName() {
            return new File(base, filename).getAbsolutePath();
	}

      public void start() {
        JBarcodeBean bb = new JBarcodeBean();
        bb.setCodeType(new ExtendedCode39());
        bb.setShowText(this.showtext);
        bb.setBarcodeHeight(barcodeHeight);
        bb.setNarrowestBarWidth(narrowestBarWidth);
	try{
            FileOutputStream fout = new FileOutputStream(new File(base, filename));
            bb.setCode(code);
            bb.gifEncode(fout);
            fout.close();
          }
          catch (Exception e) {
              Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
          }
	}
	
	public static void main(String[] args) {
		GenBarcode helper = new GenBarcode();
		if (args.length > 1) {
			helper.setBasePath(args[1]);
		}
		helper.setCode(new Integer(args[0]));
		helper.start();
		System.out.println(helper.getFileName());
	}
}

