package odl;

public class OpenflowSwitch implements Comparable<Object>{

	private String name = "";
	private int hostport = 1;
	private int MPLSport = 2;
	private int IPport = 3;
	
	public String getName() {
		return name;
	}
	public OpenflowSwitch(String name, int hostport, int mplSport, int iPport) {
		super();
		this.name = name;
		this.hostport = hostport;
		MPLSport = mplSport;
		IPport = iPport;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int gethostport() {
		return hostport;
	}
	public void sethostport(int hostport) {
		this.hostport = hostport;
	}
	public int getMPLSport() {
		return MPLSport;
	}
	public void setMPLSport(int mplSport) {
		MPLSport = mplSport;
	}
	public int getIPport() {
		return IPport;
	}
	public void setIPport(int iPport) {
		IPport = iPport;
	}
	
	public String toString() {
		
		return "OpenflowSwitch::toString name = " +this.getName()+ " IP port "+this.getIPport() + " MPLS port = " +this.getMPLSport()+ " host port " + this.gethostport(); 
		
	}
	
	public int compareTo(Object obj) throws ClassCastException {
	    if (!(obj instanceof OpenflowSwitch))
	        throw new ClassCastException("OpenflowSwitch type expected");
	    
 	        return  ((OpenflowSwitch) obj).getName().compareTo(this.getName());
	}
	
}
