public class KMP {
 public static boolean search(String text,String pattern){int[] lps=lps(pattern);int i=0,j=0;while(i<text.length()){if(text.charAt(i)==pattern.charAt(j)){i++;j++;if(j==pattern.length())return true;}else if(j>0)j=lps[j-1];else i++;}return pattern.isEmpty();}
 static int[] lps(String p){int[] a=new int[p.length()];for(int i=1,len=0;i<p.length();){if(p.charAt(i)==p.charAt(len))a[i++]=++len;else if(len>0)len=a[len-1];else i++;}return a;}
}