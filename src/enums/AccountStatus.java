package enums;

//Account status defines the possible states of a library
//Active: Member is in good standing and can borrow books 
//suspended: member has outstanding fines 
//Closed: Account has been permanently deactivated

public enum AccountStatus {
	
	//Member Account is in good standing
	//Full Access to borrowing and reservation
	
	ACTIVE,
	
	//member account is temporarily suspended 
	
	SUSPENDED,
	
	//Member account has been permanently closed
	
	CLOSED,

}
