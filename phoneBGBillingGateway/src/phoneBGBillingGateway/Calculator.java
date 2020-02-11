package phoneBGBillingGateway;

public class Calculator {
	
	private String[] argv;
	private String message;
	private String answer;
	private int exitCode;
	
	public Calculator(String[] argv1) {
		this.argv = argv1;
		this.exitCode = -1;
		this.message = "������� ������ ������. ���� �������� �� ������ �������!";
		this.answer  = "������� ������ ������. ���� �������� �� ������ �������!";
	}
	
	
	boolean prepareAnswer() {
		
	//  ������ �� ������ 1.
		if (argv==null || argv==null ) {
		    setAnswer("Reply-Message = \"[*]��� ������!\"");
		    setExitCode(-1);
		    setMessage("$date ������ �� ������ (������ ������ � �������� �������).Reply-Message = ��� ������!\n");
		}
		else if ((argv[0].substring(0, 3)).equals("2300"))		{ // �������� � ��������� 4 ��� 3? 
		    setAnswer("Reply-Message = \"["+argv[0]+"]�y������� �� �������������!\"");
		    setExitCode(-1);
		    setMessage("$date ������ �� ������ (�y������� �� �������������!).Reply-Message = ��� ������!\n");		    
		}
		else if (argv[0].length()==0) {
		    setAnswer("Reply-Message = \"[*]�������� ��������� �������\"");
		    setExitCode(-1);
		    setMessage("$date ������ �� ������ (������ ������ � �������� �������), Reply-Message = �������� ��������� �������\n");
		}
		else if (
		// �������� �� ������ ������ �����������.
		// �������� �������������� ������ ������-�� ������������.. ������ �� �������������� ������ ��������.
					(argv[2].length() !=7)  ||			(argv[2].length() <10)  ||		(argv[2].length() !=2)  ||
					(argv[2] !="101")		||			(argv[2] !="102")		||		(argv[2] !="103")		||
					(argv[2] !="115")		||			(argv[2] !="123")		||		(argv[2] !="121")		||
					(argv[2] !="104")		||			(argv[2] !="112")		||		(argv[2] !="911")		||
					(argv[2] !="007")
				)			{
			    setAnswer("Reply-Message = \"[*]"+argv[2]+" - �������� �����.\"");
			    setExitCode(-1);
			    setMessage("$date �������� �� ������ ������ �����������.Reply-Message = "+argv[0]+" -> "+argv[2]+". �������� ����� �����������. ������ ����������.\n");
		}
		else if (isEmergency()) {
		    setAnswer("Reply-Message = \"[*]���������� �����! ������ "+argv[0]+" -> "+argv[2]+" ������.\"");
		    setExitCode(0);
		    setMessage("$date ���������� ��� ���������� ������.Reply-Message = ���������� �����! ������ "+argv[0]+" -> "+argv[2]+" ������.\n");	
		}
		else if(isAbonent())  {
			// ��� �������� �� ������ �������������. ������ ��������.
			stepTwo();
		}
		else {
		    setAnswer("Reply-Message = \"[*]������ "+argv[0]+" -> "+argv[2]+" �� �����������.\"");
		    setExitCode(-1);
		    setMessage("������ �� �����������!"+argv[0]+" -> "+argv[2]+"\n");
		}
			
		// ���� �� ������������.
		return true;
	}
	
	private void stepTwo() {
		if (!isStatusActive()) {
		    setAnswer("Reply-Message = \"[*]������ c "+argv[0]+" ���������� - ��. ������ ��������. \"";);
		    setExitCode(-1);
		    setMessage("$date Reply-Message = ������ c "+argv[0]+" ���������� - ��. ������ ��������. \n";);
		}
		else {
			boolean balance = false;
			boolean gwStatus = false;
			boolean isDirectionOpen = false;
			
//			# 1. �������� �������. �������� ������� ���������� +1, ���� ������� ���������, ��� ����������� �� ��������� �����. (!!!)
//			# ���� ������� ��������� - ������������ ��������� �����.
			if (getMode().equals("credit")) { balance = true;} else { balance = getBalance();}

//			# 2. �������� �����.
			gwStatus = chkGateway();
			
//			# 3. �������� �����������. ��� ������� ������ �������� ���������.
			if (argv[2].length() == 7)	{	isDirectionOpen = true; }	else	{	isDirectionOpen = chkDirection();	}
			
//			# �������� �� �������� ������� � 4.
//			# 4. ������ "8" �� �������� ��� ����������� ������ "������� ���������" (��� ������ �����, �������� � ������� ������ "���������". ������ "8" - ������ �������/��/�� �����.
			if (isTariffCombination()) {
			    setAnswer("Reply-Message = \"[*]������ c "+argv[0]+" ��� ��������� ������ �� ������� ����� ���������� \"");
			    setExitCode(-1);
			    setMessage("\"$date Reply-Message = ������ c $ARGV[0] ��� ��������� ������ �� ������� ����� ���������� \\n\""); 
			}
			else {
//				# ����� ������ ������ ������ 
				if (argv[2] == "007") {
				    setExitCode(-1);					
				    setAnswer("Reply-Message = \"[*]������ � �������. ������ "+argv[0]+" -> "+argv[2]+" ������.\"");
				    setMessage("$date Reply-Message = ������ � �������. ������ "+argv[0]+" -> "+argv[2]+" ������.\n"); 
				}
			}

			/// =># ���� ������ ������������� - ����������� �� ��� ������ �����������: ������� ����������, �������� � ������� �����.
// ��� �����������.			
			
		}			
	}
	
	
	private boolean isTariffCombination() {
		
		return false;
	}
	

	private boolean chkDirection() {
		
/*

# ��������� ���������� �� ����������� �����-��������-������������. 
# ���� ������ �� ������, ��� ������� ������ �����������. � �� - ������ �������. ��������� ��.
my $res=0;	my ($str,$sth1,$arg)="";
# ��������� ������ ������� �� 8, ����������� ��������
$sql = "select
	    `number_resource_use_3`.`cid` as cid,
	    group_concat(`contract_tariff`.tpid) as list
        from `number_resource_3`
	left join `number_resource_use_3`
                        on `number_resource_use_3`.`resource_id` = `number_resource_3`.`id`
        left join contract_tariff
                        on `contract_tariff`.`cid` = `number_resource_use_3`.`cid` and
	                            (`contract_tariff`.tpid IN(306,307,308,333,336,337,334,338,335,344,368,369)) and
                                    (`contract_tariff`.`date1`<= NOW()) and
                                    ((`contract_tariff`.`date2` >= NOW()) or (`contract_tariff`.`date2` is NULL))
        where
                                    (`number_resource_3`.`number` like '%$ARGV[0]') and
                                    (`number_resource_3`.`date1` <= NOW())           and
                                    ((`number_resource_3`.`date2` >= NOW()) or  (`number_resource_3`.`date2` is NULL))
                                    and (`contract_tariff`.tpid is NOT NULL)
        group by `contract_tariff`.`cid`";

	$sth1 = $dbh->prepare($sql);	if (!$sth1) { die "Error:".$sth1->errstr."\n"; }	if (!$sth1->execute) { die "Error: ".$sth1->errstr."\n"; }
	my $l=$sth1->fetchrow_hashref;    my $b = $l->{'list'};

#	�������� ������������� �����������. � ������ �������.
    my @svb = split (/\,/,$b);

# ��������� ����� ����������� � ���������� �� ��� ���� ����� �� ������� ��������.
    foreach $arg(@svb)	{	if (chkdirection($arg) == 1) { return 1;}	}

#    return 1; # ��-�� ������ �������� 2300198 �������� ���������, ��������!!!!
    return 0;
*/
		return false;
	}
	
	
// # ---------------------------------------- ����� ��������� �������� ������ ----------------------------------------------------------
	
	private boolean chkGateway() {
		
	}

	private boolean getBalance() {
		// ����������� ������ �������� � ���������
		sql = "select" +
			    "`number_resource_use_3`.`cid` as cid," +
			    "`contract_balance`.summa1 + `contract_balance`.`summa2` - `contract_balance`.`summa3` - `contract_balance`.`summa4` as balance," +
			    "`contract`.`closesumma`     as closesumma" +
			"from `number_resource_3`" +
			"left join `number_resource_use_3`	on `number_resource_use_3`.`resource_id` = `number_resource_3`.`id`" +
			"left join `contract_balance` " +
			"		on	(`contract_balance`.`cid` = `number_resource_use_3`.`cid`) and" + 
			"			(`contract_balance`.`yy` = year(NOW())) and"  +
			"			(`contract_balance`.`mm` = MONTH(NOW()))" +
			"left join `contract`	on `number_resource_use_3`.`cid`=`contract`.`id`" +
			"where " +
			"	(`number_resource_3`.`number` like '%"+argv[0]+"')	and" + 
			"	(`number_resource_3`.`date1` < NOW())		and " +
			"	((`number_resource_3`.`date2` > NOW()) or  (`number_resource_3`.`date2` is NULL))";
/*
 # ��������� ������������ ������� � ������ �� ��������.
my $limit = $l->{'closesumma'};
my $b = $l->{'balance'};
#
if ($b >= 0)	{return 1;}	else 	{ if ($b>=$limit) {return 1;} else { return -1; }  }
 */
		return false;
	}

	
	private String getMode() {
		// �������� �������� ������ - ���������, ���������?		
		String sql = "SELECT contract.mode as mode" +
				"FROM number_resource_3 AS res" +
				"LEFT JOIN number_resource_use_3 AS ruse ON res.id=ruse.resource_id AND ruse.date1<= CURDATE() AND (ruse.date2 is null or ruse.date2>=CURDATE() or ruse.date_reserve>=CURDATE())" +
				"LEFT JOIN contract ON contract.id=ruse.cid" +
				"WHERE (1>0) and (number like '%"+argv[0]+"%') and (contract.title is not NULL)";

/*
my $r=$sth1->fetchrow_hashref;	my $r1 = $r->{'mode'};
if ($r1 eq '1')	{	return 'debet'	}	else	{	return 'credit';	}
*/		
		//return "debet";
		return "credit";
	}
	
	
	
	
	private boolean isStatusActive() {
		String sql = "SELECT `contract`.`status` as status" + 
					"FROM number_resource_3 AS res" +
					"LEFT JOIN number_resource_use_3 AS ruse ON res.id=ruse.resource_id AND ruse.date1<= CURDATE() AND (ruse.date2 is null or ruse.date2>=CURDATE() or ruse.date_reserve>=CURDATE())" +
					"LEFT JOIN contract ON contract.id=ruse.cid" +
					"WHERE (contract.title is NOT NULL) and (1>0) and (number like '%"+argv[0]+"%')";
//		    if (($limit == 1) or ($limit == 2) or ($limit == 3) or ($limit == 4) or ($limit == 7) or ($limit == 8) or ($limit == 9))     {$fl = 1;}     else {$fl =0;}
//		    return $fl;
					
		return false;		    
	}
	

	private void setExitCode(int exitCode1) {
		this.exitCode = exitCode1;
	}
	
	private void setMessage(String message1) {
		this.message = message1;
	}
	
	private void setAnswer(String answer1) {
		this.answer = answer1;
	}
	
	String getAnswer() {
		return this.answer;
	}
	
	String getMessage() {
		return this.message;
	}
	
	int getExitCode() {
		return this.exitCode;
	}
	





private static void continueAlaniz() {

}


//========================================================================================================

private boolean isAbonent() {
//  �������� 0 - ����� �������� �� ���������, 1 - ����� �������� � �� ��������.
String sql = "SELECT count(*) as sum" +
	    "FROM number_resource_3 AS res" +
	    "LEFT JOIN number_resource_use_3 AS ruse ON res.id=ruse.resource_id AND ruse.date1<= CURDATE() AND (ruse.date2 is null or ruse.date2>=CURDATE() or ruse.date_reserve>=CURDATE())"+
	    "LEFT JOIN contract ON contract.id=ruse.cid"+
	    "WHERE (contract.title is NOT NULL) and (1>0) and (number like '%"+argv[0]+"%')";
// 0 - false; 1 - true;
return false;
}

private boolean isEmergency() {

if ((argv[2].length()==2) && (argv[2].substring(0, 1).equals("0"))) { return true; } 

switch(argv[2]) {
	case "911":	return true; 
	case "112":	return true; 
	case "101":	return true; 
	case "102":	return true; 
	case "103":	return true; 
	case "104":	return true; 
	case "121":	return true; 
	case "123":	return true; 
	case "115":	return true; 
	
	default: return false;
} 




/*		else if (isAbonent()) {
continueAnaliz();
}
System.out.print("Reply-Message = \"[*]����� $ARGV[0] �� ������� �� ������ ��������. ������ ����������\"");
System.exit(-1);
// LOG "$date ���������� ����� �� �������� ��� ���?Reply-Message = ����� $ARGV[0] �� ������� �� ������ ��������. ������ ����������.\n";

*/