package phoneBGBillingGateway;

public class Calculator {
	
	private String[] argv;
	private String message;
	private String answer;
	private int exitCode;
	
	public Calculator(String[] argv1) {
		this.argv = argv1;
		this.exitCode = -1;
		this.message = "неясная ошибка данных. сюда попадать не должно никогда!";
		this.answer  = "неясная ошибка данных. сюда попадать не должно никогда!";
	}
	
	
	boolean prepareAnswer() {
		
	//  защита от дурака 1.
		if (argv==null || argv==null ) {
		    setAnswer("Reply-Message = \"[*]Нет данных!\"");
		    setExitCode(-1);
		    setMessage("$date защита от дурака (пустой запрос и странные символы).Reply-Message = Нет данных!\n");
		}
		else if ((argv[0].substring(0, 3)).equals("2300"))		{ // путаница с индексами 4 или 3? 
		    setAnswer("Reply-Message = \"["+argv[0]+"]нyмерация не обслуживается!\"");
		    setExitCode(-1);
		    setMessage("$date защита от дурака (нyмерация не обслуживается!).Reply-Message = Нет данных!\n");		    
		}
		else if (argv[0].length()==0) {
		    setAnswer("Reply-Message = \"[*]Неверные параметры запроса\"");
		    setExitCode(-1);
		    setMessage("$date защита от дурака (пустой запрос и странные символы), Reply-Message = Неверные параметры запроса\n");
		}
		else if (
		// проверка по длинне номера направления.
		// проверка междугороднего номера почему-то неправильная.. почему то проглатывается первая восьёрка.
					(argv[2].length() !=7)  ||			(argv[2].length() <10)  ||		(argv[2].length() !=2)  ||
					(argv[2] !="101")		||			(argv[2] !="102")		||		(argv[2] !="103")		||
					(argv[2] !="115")		||			(argv[2] !="123")		||		(argv[2] !="121")		||
					(argv[2] !="104")		||			(argv[2] !="112")		||		(argv[2] !="911")		||
					(argv[2] !="007")
				)			{
			    setAnswer("Reply-Message = \"[*]"+argv[2]+" - странный номер.\"");
			    setExitCode(-1);
			    setMessage("$date проверка по длинне номера направления.Reply-Message = "+argv[0]+" -> "+argv[2]+". Странный номер направления. Звонок невозможен.\n");
		}
		else if (isEmergency()) {
		    setAnswer("Reply-Message = \"[*]Экстренный вызов! Вектор "+argv[0]+" -> "+argv[2]+" открыт.\"");
		    setExitCode(0);
		    setMessage("$date пропускаем все экстренные вызовы.Reply-Message = Экстренный вызов! Вектор "+argv[0]+" -> "+argv[2]+" открыт.\n");	
		}
		else if(isAbonent())  {
			// тут проверки на дурака заканчиваются. анализ контента.
			stepTwo();
		}
		else {
		    setAnswer("Reply-Message = \"[*]Вектор "+argv[0]+" -> "+argv[2]+" не допускается.\"");
		    setExitCode(-1);
		    setMessage("Вектор не допускается!"+argv[0]+" -> "+argv[2]+"\n");
		}
			
		// пока не используется.
		return true;
	}
	
	private void stepTwo() {
		if (!isStatusActive()) {
		    setAnswer("Reply-Message = \"[*]Звонок c "+argv[0]+" невозможен - см. статус договора. \"";);
		    setExitCode(-1);
		    setMessage("$date Reply-Message = Звонок c "+argv[0]+" невозможен - см. статус договора. \n";);
		}
		else {
			boolean balance = false;
			boolean gwStatus = false;
			boolean isDirectionOpen = false;
			
//			# 1. проверка баланса. проверка баланса возвращает +1, если договор кредитный, вне зависимости от состояния счёта. (!!!)
//			# если договор авансовый - возвращается состояние счёта.
			if (getMode().equals("credit")) { balance = true;} else { balance = getBalance();}

//			# 2. проверка шлюза.
			gwStatus = chkGateway();
			
//			# 3. проверка направления. при местном вызове проверка отключена.
			if (argv[2].length() == 7)	{	isDirectionOpen = true; }	else	{	isDirectionOpen = chkDirection();	}
			
//			# отсекаем по критерию правила № 4.
//			# 4. Услуга "8" не работает без подключения услуги "Местная телефония" (или пакета услуг, имеющего в составе услугу "Телефония". Услуга "8" - услуги зоновой/МГ/МН связи.
			if (isTariffCombination()) {
			    setAnswer("Reply-Message = \"[*]Звонок c "+argv[0]+" без установки тарифа на местную связь невозможен \"");
			    setExitCode(-1);
			    setMessage("\"$date Reply-Message = Звонок c $ARGV[0] без установки тарифа на местную связь невозможен \\n\""); 
			}
			else {
//				# здесь разные кривые номера 
				if (argv[2] == "007") {
				    setExitCode(-1);					
				    setAnswer("Reply-Message = \"[*]Звонок в справку. Вектор "+argv[0]+" -> "+argv[2]+" открыт.\"");
				    setMessage("$date Reply-Message = Звонок в справку. Вектор "+argv[0]+" -> "+argv[2]+" открыт.\n"); 
				}
			}

			/// =># если баланс отрицательный - доппроверка на тип номера направления: местные пропускаем, межгород и сотовую рубим.
// тут продолжение.			
			
		}			
	}
	
	
	private boolean isTariffCombination() {
		
		return false;
	}
	

	private boolean chkDirection() {
		
/*

# проверяем разрешение на направления город-межгород-международка. 
# пока только всё вместе, без разбора номера направления. и то - только смотрим. разрешаем всё.
my $res=0;	my ($str,$sth1,$arg)="";
# формируем список тарифов на 8, разрешённых абоненту
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

#	проверим разрешённость направления. с каждым тарифом.
    my @svb = split (/\,/,$b);

# проверяем номер направления и пропускает ли его один любой из тарифов абонента.
    foreach $arg(@svb)	{	if (chkdirection($arg) == 1) { return 1;}	}

#    return 1; # из-за ошибки сномером 2300198 проверка отключена, включить!!!!
    return 0;
*/
		return false;
	}
	
	
// # ---------------------------------------- Конец алгоритма пропуска вызова ----------------------------------------------------------
	
	private boolean chkGateway() {
		
	}

	private boolean getBalance() {
		// вытаскиваем баланс абонента с договором
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
 # проверяем соответствие баланса и лимита на договоре.
my $limit = $l->{'closesumma'};
my $b = $l->{'balance'};
#
if ($b >= 0)	{return 1;}	else 	{ if ($b>=$limit) {return 1;} else { return -1; }  }
 */
		return false;
	}

	
	private String getMode() {
		// проверка договора номера - кредитный, дебетовый?		
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
//  Заглушка 0 - номер закреплён за абонентом, 1 - номер свободен в БГ биллинге.
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
System.out.print("Reply-Message = \"[*]Номер $ARGV[0] не выделен ни одному абоненту. Звонок невозможен\"");
System.exit(-1);
// LOG "$date распределён номер на абонента или нет?Reply-Message = Номер $ARGV[0] не выделен ни одному абоненту. Звонок невозможен.\n";

*/