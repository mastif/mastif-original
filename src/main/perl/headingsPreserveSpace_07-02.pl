#!/usr/local/bin/perl  # only needed on Unix systems

################################################################################################################
# headingsPreserveSpace.pl
# Cheryl Clark
# The MITRE Corporation
#
# May, 2010   Improvements: more general input filename recognition; additional headings recognized
#
# June 29, 2010 bug fixes to: PHYSICAL EXAM
#
# July 2, 2010  Improvements to Section Introduction patterns for sections iwthout headings.  Using non-greedy (i.e., minimal matching) quanitifers ( +? )
# in these substitution patterns.  With longest match (default), we often start the new section too late because a sentence further along in the section
# also matches the pattern.  We want to start the new section at the first match # after the metadata section, and this requires a non-gready match

#
# This script identifies section headings and sections in structured clinical input.  It is derived from a script
# that takes an ASR output text file as input and identifies sections and section headings and subheadings
# assuming no formatting such as carriage returns in the input.
#
# This version has been modifed to run on structured documents. It does not identify list stucture.
# This scipt is designed to preserve the spacing of the input file (including carriage returns/line feeds) and only inserts xml tags,
# so that inlined annotation can be convertted to standoff representation.
# It is derived from  headings container8.pl. which inserts carriage returns to make the output more human reader-friendly.
#
# The script identifies structure in four passes and produces 4 output files:
#
#          Processing                                           Input File                         Output File
#
# Pass 1.  Reformat input                                       <input filename>.<input extension> <input filename>.out1
#          (replace xml entities with text,
#          replace line breaks with space character,
#          tag document headers)
# Pass 2.  Identify section headings and sections               <input filename>.out1              <input filename>.out2
#
# Pass 3.  Identify subsection headings and subsections.        <input filename>.out2              <input filename>.out3
#  
# Pass 4.  Close subsections (Add </subsection> tags)           <input filename>.out3              <input filename>.out4
#
################################################################################################################

##headings
#HISTORY/SUBJECTIVE 
$subject1 = 'subjective';
$subject2 = 's';
$chcom = '(?:chief\s+complaint)|(?:patient\s+states\s+complaint)';
$reascons = 'reason\s+(?:for\s+)?consultation';
$reashosp = '(?:(?:history\s+and\s+)?reasons?\s+(?:for\s+)?(?:hospitalization|admission|admit))';
$hpi1 ='(?:history\s+(?:of\s+(?:the\s+)?)?(?:present|physical)\s+illness|hpi)';
$hpi2 = '(?:present\s+illness)';
$hpi3 = '(?:clinical\s+)?history';
$pmh = 'past\s+(?:medical\s+)?history|medical\s+history|pmhx?|pmedhx';
$psh = 'past\s+surgical\s+history';
$pch = 'past\s+cardiac\s+history';
$obh = '(?:past\s+obstetric\s+history)';
$gynh = '(?:past\s+gyn(?:ocologic(?:al)?)?\s+history)';
$obgynh = '(?:(?:past\s+)?ob\-gyn\s+history)';
$ros = 'review\s+(?:of\s+)?systems|ros';
#$alg1 = 'allergies|allergy|all|adverse\s+drug\s+reactions?';
$alg1 = 'allergies|allergy|adverse\s+drug\s+reactions?';
$alg2 = 'allergy(?:\s*\/\s*reaction)?\s+profile';                   #    Allergy / reaction profile :
$fh = '(?:family\s+(?:(?:medical|social)\s+)?history|fhx?|fam\s+hx)';
$sh = 'social\s+history|social\s+hx|\bshx?|soc\s*hx|habits';



#EXAM/LAB/OBJECTIVE
$object1 = 'objective';
$object2 = 'o';
$phe = '(?:physical|surgical)(?:\s+exam(?:ination)?|\s+findings)?|\bpe|\*pe|on\s+exam(?:ination)?|examination\s+data';
$status = '(?:daily|patient)\s+status';
$lab = '(?:summary\s+of\s+)?(?:(?:diagnostic\s+)?(?:laboratory|labs?|laboratories|diagnostic|radiologic)(?:(?:\s+and\s+)|(?:\/))?\s*(?:radiologic\s+)?(?:studies|data|results|exams?(?:inations?)?|evaluation|values|findings|tests?|x\-rays?)?)';
$tst = '(?:tests\/procedures|(?:studies\/(?:procedures|tests))|(?:proc(?:edures)?\/tests)|tests)';
$clfnd = 'clinical\s+findings?';
$rad = 'radiology(?:\s+(?:studies|data|results))?';
$rslt = '(?:new|discrete|(?:today\s+\'s))\s+results(?:\s+only)?';


#ASSESSMENT
$asp = '(?:assessment\s*(?:and|\&amp\;|\/)\s*plan)|(?:a\s*(?:\&amp\;|\/)\s*p)';
$asm = 'assessment|a\s*(:|colon)';
$dspl = '(?:disposition\s*(?:and|\&amp\;|\/)\s*plan)|(?:disposition\s*(?:\&amp\;|\/)plan)';
$dsp = 'disposition';
$cnd = 'condition';
#$pln = '\bplans?|\bp';
$pln = '\bplans?';
$tdpln = '(?:to\s+do\s*\/\s*plans?)';
$imppln = '(?:impression\s+and\s+plan)';
$imp = 'impressions?|imp';
$cmp = 'complications?';


$inst = 'instructions?';
$cod = 'code(?:\s+status)?';
$appt = 'appointments?(?:\(?:\s+s\s+\))?';

$hosp = '(?:hosp(?:ital)?\s+course(?:\s+and\s+treatment)?)|(?:summary\s+of\s+hospitalization)';
$ed = '(?:ed\s+course)|(?:emergency\s+department\s+course)|(?:in\s+(?:the\s+)?ed|\bed)';
$evnt = 'events';

#DIAGNOSIS
$diag = '(?:cause\s+of\s+death\s+and\s+)?((?:final|principal|principle|primary|associated|secondary|additional|other)\s+)?(((?:pre-)?admission|admit(?:ting)?|discharge|(?:pre)?operative)\s+)?(?:problems\s+and\s+)?diagnos[ei]s(?:\s*\(\s*es\s*\))?';

$diag2 = '(?:principal\s+discharge\s+diagnosis\s*\;\s*responsible\s+after\s+study\s+for\s+causing\s+admission\s*\)\s?)';

$diag3 = '(?:other\s+diagnosis\s*\;\s*conditions\s*\,\s*infections\s*,\s*complications\s*,\s*affecting\s+treatment\s*\/?\s*stay)';

#$dd = '(principal|principle|primary)\s+discharge\s+diagnos[ei]s';
#$pd = '(principal|principle|primary)\s+diagnos[ei]s';
#$asd = 'associated\s+(discharge\s+)?diagnos[ei]s';
#$sd = 'secondary\s+diagnos[ei]s';
#$ad = '((pre-)?admission|admit(ting)?)\s+diagnos[ei]s';
#$preopd = '(pre)?operative\s+diagnos[ei]s';

#$od = 'other\s+(problems\s+and\s+)?diagnos[ei]s';
$op = 'other\s+(?:assciated\s+)?problems';

# $prob = 'problems|problem\s+list';
$prob = '(?:(?:patient\s+)?problem\s+list)|(?:significant\s+problems)|(?:(?:(?:his|her)\s+)?problems\s+and\s+management\s+are\s+as\s+follows) ';

#PROCEDURE/TREATMENT
$proc1 = '(?:((?:principal|principle|primary|special)\s+)?(?:operations?\s+(?:and|or)\s+)?procedures?(\s+(?:and|or)\s+operations?)?(?:\s+performed)?)';
$proc2 = 'operations\s*\/\s*procedures';
$proc3 = 'operations?';
$proc4 = '((?:associated|secondary|additional|other)\s+)(?:operations?\s+(?:and|or)\s+)?procedures?(\s+(?:and|or)\s+operations?)?';
$proc5 = 'procedure\s+note';
$treat1 = 'other\s+treatments?\s+and\s+procedures';
$treat2 = '(?:other\s+treatments?\s*\/\s*procedures\s+\(\s+not\s+in\s+o\.r\.\s+\))';
$treat3 = '(?:treatment\s+rendered)';

$pp_proc_diag = 'postpartum\s+diagnostic\s+procedures';
$pp_proc_ther = 'postpartum\s+therapeutic\s+procedures';
$pp_proc_oth = 'other\s+postpartum\s+therapies'; 


#DISCHARGE/DISPOSITION/FOLLOW-UP
$dis_wnd = 'discharged?\s+wound\s+care';
$dis_fol1 = '(?:discharged?\s+follow[\s\-]?up(?:\s+care)?)';
$dis_fol2 = '(?:standardized\s+)?discharge\s+orders\s+\(\s+medications\s+instructions\s+to\s+patient\s+\,\s+follow-up\s+care\s+\)';
$dis_fol3 = 'disposition\s*\,\s+follow[\s+\-]up\s+and\s+instructions\s+to\s+patient';
$dis_act = '(?:discharge\s+)?activity';
$dis_ord = '(?:doctor\'?s\s+)?discharge\s+orders';
$med1 = 'medications?|medication\s*\(\s*s\s*\)|regimen|treatment\s+cycle';
$med2 = 'meds?';
$follup = '(?:follow[\s\-]?up)';
$diet = 'diet';
#$act = 'activity';
$work = 'return\s+to\s+work';
$addend = 'addendum';
$prevcard = 'previous\s+cardiovascular\s+interventions?';
$trans = '(?:escription\s+document)|batch';
$preopst = 'preoperative\s+(?:cardiac\s+)?status';

#metadata
$dict1 = '(?:(?:(?:entered|dictated)\s+by\b)|dictator)';
$dict2 = '(?:(?:[A-Z]+\.?)\s+)*[A-Z]+\s*\,\s+M\.D\.\s+DICTATING\s+FOR';
$dict3 = 'This\s+report\s+was\s+created\s+by';
$cons1 = 'consultants?';
$cons2 = 'consults';
$hst = 'health\s+status';

$BE = '\s+(?:am|is|are|was\were)';
$INCLUDE = '\s+(?:include[ds]?)';

#heading modifiers
$rh_org_mod = '\s+(?:(?:by\s+(?:problems?|(?:organ\s+)?systems?|issues?|report))|(?:\s+of\s+note))';
$lh_org_mod = '(?:brief(?:\s+resume\s+of)?|overall|other|additional|pertinent|relevant|(?:list\s+of\s+)?other|plan\s+for|standardized|summary\s+of|doctor(?:\s+\&apos\;)?s)\s+';
#$rh_temp_mod = '\s+(?:(?:(?:up)?on|at|\@|for)\s+(?:(?:(?:the|that)\s+)?time\s+of\s+)?(?:admission|admit|discharge|transfer|complications?|home))';
$rh_temp_mod = '\s+(?:(?:(?:UP)?ON|AS|AT|\@|OF|FOR|(?:PRIOR\s+TO)|(?:DURING))\s+(?:(?:(?:THE|THAT|THIS)\s+)?(?:TIME|DAY)\s+OF\s+)?(?:ADMISSION|ADMIT|DEATH|DISCHARGE|TRANSFER|COMPLICATIONS?|HOME|HOSPITALIZATION|OUTPATIENT))';
$lh_temp_mod = '(?:(?:pre-)?(?:admit(?:ting)?|admission)|current|discharge|follow[\s+\-]?up|home|interim|initial|postpartum|(?:pre|post)op(?:erative)?|transfer)\s+';
$rh_loc_mod = '\s+in\s+the\s+emergency\s+department';

#subheadings
$gen = '(?:\bgen\b|general(\s+(?:appearance|exam))?)';
$heent = '(?:heent|(?:head\s+and\s+neck))';
$neuro = '(?:neuro(?:logic(?:al)?)?|neurology)';
$cardiac = 'cardiac';
$cardiov = '(?:(?:cardio)?vascular|cvs?)';
$coron = 'coronary';
$resp ='respiratory|respirations|resp';
$gi = '(?:\bgi\b|gastrointestinal)';
$gu = '\bgu\b';
$hemonc = 'hematologic\s+oncologic';
$hem = '(?:hematologic|hematology|heme)';
$onc = 'oncologic';
$musk = '(?:general\s+)?musculoskeletal';
$psych = 'psych(?:iatric|iatry)?';
$vital = '(?:vital\s+signs|\bvs|(?:triage|admission)?vitals)';
$heart = 'heart';
$neck = '\bneck';
$chstlng = '(?:chest\s+and\s+lungs)|(?:chest\s*\/\s*lungs)'; 
$chst = 'chest';
$lng = 'lungs';
$abdo = '(?:abdomen|abdm?|abdominal)';
$extr = 'extremities|ext';
$skin = 'skin|derm(?:atology)?';
$pulm = 'pulmonary|pulm';
$rect = 'rectal';
$mst = 'mental\s+status';
$rnlfen = 'renal\/fen';
$rnl = 'renal';
$fen = '(?:fen|fluids\s*\,*\s*electrolytes(\s*\,*\s+(?:and\s+)?)?nutrition)';
$nutr = 'nutrition';
$endo = '(?:endo(?:crine|ology)?)';
$inf1 = '(?:infectious\s+diseases?)';
$inf2 = '(?:id)';
$pain ='pain';
$rheum = 'rheumatolog(?:ic|y)';
$proph = '(?:prophylaxis|ppx)';
$cc = 'cc';
$dx = 'dx';
$ddx = 'ddx';
$idx = 'id\/dx';
$dispo = 'dispo';
$ortho = 'ortho';
$gyn = 'gyn(?:ecology)?';

$admitdate = '(?:(?:admission\s+date)|(?:date\s+of\s+admission))';
$dischdate = 'discharge\s+date|date\s+of\s+discharge';
$evaldate = 'evaluation\s+date';
$dob = '(?:(?:date\s+of\s+birth)|DOB)';
$expdate = '(date\s+of\s+expiration)';
$age = 'age';
$sex = 'sex';
$ddetails = 'discharge\s+patient\s+on';
$rptsts = 'report\s+status';
$attmd = 'attend(?:ing)?(?:\s+physician)?';
$srv = 'service';
$cmt = '(?:additional\s+)comments';
$pat_info = '\*+\s+(FINAL\s+)?DISCHARGE\s+ORDERS\s+\*+';
$pcp = 'pcp\s+name';
$prelim = 'preliminary\s+report';
$prvnum = 'provider\s+number';
$untnum = 'unit\s+number';
$dis_notif = 'ed\s+discharge\s+notification\s*\/\s*summary';

$section_intro1 = '(?:(?:I\s+(?:personally\s+)?(?:examined|interviewed|saw|managed))|(?:I\s+am\s+(?:currently\s+)?(?:managing|seeing))|(?:I\s+was\s+asked))|(?:I\s+am\s+continuing\s+to\s+(?:manage|see))';
$section_intro2 = 'management\s+will\s+be';
$section_intro3 = '(?:(?:the\s+)?patient\s+was\s+(?:brought|admitted))';
$section_intro4 = '(?:(?:(?:overnight|subjectively)\s+(?:\,\s+)?)?(?:the\s+patient)|Mrs?\.\s+\S+\s+is)';
$

$lstnum = 'number\s+(\d+|one|two|three|four|five|six|seven|eight|nine|ten)';
$k = 0;
$list_flag = 'false';

$report_type = 'Unknown';
$section = '';
$subsection = '';



if($#ARGV == 0){
    $input_file = $ARGV[0];
#    $source_srt_dir = $ARGV[1];
#    $target_align_dir = $ARGV[2];
}
else{
    print "\nUsage:  perl headingsPreserveSpace.pl <input file name>\n";
    die;
}


#First pass through the input: formatting adjustments

if ($input_file =~ m/(([a-z,A-Z]\:\\)?[\.,\\,a-z,A-Z,\d,_,\-]+)/) {
  $file_name = $1;
  print "File name is $file_name\n\n";
#  $file_extension = $2;
}

$output_file = "$file_name" . ".out1";

open INPUT, "$input_file" or die "Cannot open input file $input_file\n";
open OUTPUT, ">$output_file" or die "Cannot open output file $output_file\n";

 undef $/;
 $line = <INPUT>;

# while ($line = <INPUT>)
# {

 # Replace XML entities with text

#    $line =~s/\&amp\;/&/g;
#    $line =~s/\&apos\;/'/g;       #'
#   $line =~s/\&gt;/>/g;
#    $line =~s/\&lt;/</g;
#    $line =~s/\&quot;/"/g;        #"
 # Count documents in container of multiple documents


#    if ($line =~ m/\<doc id/i) {
#    $doc_counter++;
#    }



#    if ($line =~m/\d{9,9}\s+\|.+/) {       # # Wrap header found in In i2b2 2008 container file
#       $line =~  s/(\d{9,9}\s+\|.+)/\<section ID=\'22\' type=\'HEADER\'\>\<text\>$1\<\/text\>\n\<\/section\>/;
#       $line =~  s/(\d{9,9}\s+\|.+)/\<section ID=\'22\' type=\'HEADER\'\>\<title\>\<\/title\>\<text\>$1\<\/text\>/;
#    }
    if  ($line =~m/^(?:PROGRESS\s*)?\*+INSTITUTION(\s+(?:GENERAL|CRITICAL\s+CARE)\s+MEDICINE\s+ATTENDING\s+PHYSICIAN\s+PROGRESS\s+NOTE)?/) {    # Header in UPMC deidientified progress notes
           $line =~ s/^((?:PROGRESS\s*)?\*+INSTITUTION(\s+(?:GENERAL|CRITICAL\s+CARE)\s+MEDICINE\s+ATTENDING\s+PHYSICIAN\s+PROGRESS\s+NOTE)?)/\<section ID=\'22\' type=\'HEADER\'\>\<title\>\<\/title\>\<text\>$1/;
     }
    else  {
        unless (($line =~m/^(?:final\s+)?diagnos/i)|| ($line =~ m/^admission/i) || ($line =~ m/^the\s+patient/i)) {
            $line =~ s/(^[^\n]+)/\<section ID=\'22\' type=\'HEADER\'\>\<title\>\<\/title\>\<text\>$1/;
        }
    }

#  Replace line breaks with spaces within body of document
#    unless (($line =~ m/\<\/?\w+/) || ($line =~m/\d{9,9}\s+\|.+/)) {
#             $line =~ s/\n/ /g;
#    }



#    print "To output: $line\n";

    print OUTPUT "$line";

#} # WHILE

close INPUT;
close OUTPUT;

# print "Reformatted $doc_counter documents\n";

# Second pass through medical record: Look for heading keywords and add section structure

$input_file = "$file_name" . ".out1";
$output_file = "$file_name" . ".out2";

open INPUT, "$input_file" or die "Cannot open input file $input_file\n";
open OUTPUT, ">$output_file" or die "Cannot open output file $output_file\n";

# print "Finding headings and adding section structure\n";


 undef $/;
 $line = <INPUT>;

# while ($line = <INPUT>)
# {
#      $line =~ s/(\<\/?text\>\n)//;

#    if ($line =~m/\d{9,9}\s+[A-Z]+/) {  # # Insert opening section and text tags in documents that have no xml tags (i2b2 2010 individual files)
##       chomp $line;
#       $line =~ s/(\d{9,9}\s+)/\<section ID=\'22\' type=\'HEADER\'\>\<title\>\<\/title\>\<text\>$1/;
#    }


# Find headings based on key words and phrases

    if ($line =~ m/$dischdate/i) {
      $line =~ s/(($dischdate)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'15\' type='DISCHARGE DATE'\>\<title\>$1\<\/title\>\<text\>/ig;

    }

    if ($line =~ m/$evaldate/i) {
      $line =~ s/(($evaldate)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='EVALUATION DATE'\>\<title\>$1\<\/title\>\<text\>/ig;

    }

    if ($line =~ m/$expdate/i) {
      $line =~ s/(($expdate)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='DATE OF EXPIRATION'\>\<title\>$1\<\/title\>\<text\>/ig;

    }

    if ($line =~ m/$dob/i) {
      $line =~ s/(($dob)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='DATE OF BIRTH'\>\<title\>$1\<\/title\>\<text\>/ig;

    }

    if ($line =~ m/$age/i) {
      $line =~ s/(($age)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='AGE'\>\<title\>$1\<\/title\>\<text\>/ig;

    }
    if ($line =~ m/$sex/i) {
      $line =~ s/(($sex)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='GENDER'\>\<title\>$1\<\/title\>\<text\>/ig;

    }


    if ($line =~ m/$attmd/i) {
      $line =~ s/(($attmd)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'05\' type='ATTENDING MD'\>\<title\>$1\<\/title\>\<text\>/ig;

    }

    elsif ($line =~ m//) {     
       $line =~ s/([A-Z]{2,2}\d{3,3}\/\d{4,4}\s+[A-Z]+\s+([A-Z]\.\s+)?[A-Z]+\s+\,(?:\s+(?:jr|iii)\s*\,\s*)?\s+M\.D\.)/\<\/text\>\<\/section\>\<section ID=\'05\' type='ATTENDING MD'\>\<title\>\<\/title\>\<text\>$1/ig;

    }

    if ($line =~ m/$srv/i) {
      $line =~ s/(($srv)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'38\' type='SERVICE SPECIALTY'\>\<title\>$1\<\/title\>\<text\>/i;

    }

     if ($line =~ m/$pcp/i) {
      $line =~ s/(($pcp)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'05\' type='PRIMARY CARE PHYSICIAN'\>\<title\>$1\<\/title\>\<text\>/i;

    }

    if ($line =~ m/$prvnum/i) {
      $line =~ s/(($prvnum)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='PROVIDER NUMBER'\>\<title\>$1\<\/title\>\<text\>/i;

    }

    if ($line =~ m/$untnum/i) {
      $line =~ s/(($untnum)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='UNIT NUMBER'\>\<title\>$1\<\/title\>\<text\>/i;

    }


    if ($line =~ m/$chcom/i) {
      $line =~ s/(($chcom)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'06\' type='CHIEF COMPLAINT'\>\<title\>$1\<\/title\>\<text\>/i;

    }


    if ($line =~ m/$reascons/i) {
      $line =~ s/(($reascons)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'36\' type='REASON FOR CONSULTATION'\>\<title\>$1\<\/title\>\<text\>/i;
    }



    if ($line =~ m/($hpi1\s+and\s+$reashosp)/i) {
      $line =~ s/(($lh_org_mod)?($hpi1)\s+and\s+($reashosp)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='HISTORY OF PRESENT ILLNESS\/REASON FOR HOSPITALIZATION'\>\<title\>$1\<\/title\>\<text\>/ig;
    }
    else {

        if ($line =~ m/$reashosp/i) {
          $line =~ s/(($reashosp)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'55\' type='REASON FOR HOSPITALIZATION'\>\<title\>$1\<\/title\>\<text\>/i;
        }

        if ($line =~ m/$hpi1\s*\:/i) {
          $line =~ s/(($lh_org_mod)?($hpi1)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'23\' type='HISTORY OF PRESENT ILLNESS'\>\<title\>$1\<\/title\>\<text\>/ig;
        }
        elsif ($line =~ m/$hpi1\s*\n/i) {
          $line =~ s/((?:$hpi1)\s*)(\n)/\<\/text\>\<\/section\>\<section ID=\'23\' type='HISTORY OF PRESENT ILLNESS'\>\<title\>$1\<\/title\>$2\<text\>/ig;
        }
        elsif ($line =~ m/$hpi2/i) {
          $line =~ s/(($hpi2)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'23\' type='HISTORY OF PRESENT ILLNESS'\>\<title\>$1\<\/title\>\<text\>/ig;
        }
        elsif ($line =~ m/$hpi3/i) {
          $line =~ s/(^($lh_org_mod)?($hpi3)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'23\' type='HISTORY OF PRESENT ILLNESS'\>\<title\>$1\<\/title\>\<text\>/im;
        }

     }


    if ($line =~ m/$subject1/i) {
          $line =~ s/($subject1\s*\:?)(\n)/\<\/text\>\<\/section\>\<section ID=\'\' type='SUBJECTIVE'\>\<title\>$1\<\/title\>$2\<text\>/i;
    }


    if ($line =~ m/^$subject2/im) {
          $line =~ s/(^$subject2\s*\:)(\n)/\<\/text\>\<\/section\>\<section ID=\'\' type='SUBJECTIVE'\>\<title\>$1\<\/title\>$2\<text\>/im;
    }


    if ($line =~ m/$object1/i) {
          $line =~ s/($object1\s*\:?)(\n)/\<\/text\>\<\/section\>\<section ID=\'\' type='OBJECTIVE'\>\<title\>$1\<\/title\>$2\<text\>/i;
    }


    if ($line =~ m/^$object2/im) {
          $line =~ s/(^$object2\s*\:)(\n)/\<\/text\>\<\/section\>\<section ID=\'\' type='OBJECTIVE'\>\<title\>$1\<\/title\>$2\<text\>/im;
    }



    if ($line =~ m/$pmh\s*\:/i) {
      $line =~ s/(($pmh)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'30\' type='PAST MEDICAL HISTORY'\>\<title\>$1\<\/title\>\<text\>/ig;
        }

    if ($line =~ m/$psh\s*\:/i) {
      $line =~ s/(($psh)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'31\' type='PAST SURGICAL HISTORY'\>\<title\>$1\<\/title\>\<text\>/ig;
    }
    if ($line =~ m/(?:$obgynh)\s*\:/i) {
      $line =~ s/(($obgynh)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='OB-GYN HISTORY'\>\<title\>$1\<\/title\>\<text\>/ig;
    }
    else {
      $line =~ s/(($obh)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='OBSTETRIC HISTORY'\>\<title\>$1\<\/title\>\<text\>/ig;
      $line =~ s/(($gynh)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='GYNOCOLOGIC HISTORY'\>\<title\>$1\<\/title\>\<text\>/ig;
    }

    if ($line =~ m/$fh\s*\:/i) {
      $line =~ s/(($fh)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'20\' type='FAMILY HISTORY'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$fh\s+/i) {
      $line =~ s/(FAMILY\s+HISTORY)(\s+[a-z]+)/\<\/text\>\<\/section\>\<section ID=\'20\' type='FAMILY HISTORY'\>\<title\>$1\<\/title\>\<text\>$2/;
    }


    if ($line =~ m/(?:$pmh)\/(?:$sh)\s*\:/i) {
      $line =~ s/(($pmh)\/($sh)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'30\' type='PAST MEDICAL HISTORY'\>\<title\>$1\<\/title\>\<text\>/ig;
    }
    elsif ($line =~ m/$sh\s*\:/i) {
      $line =~ s/(($sh)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'40\' type='SOCIAL HISTORY'\>\<title\>$1\<\/title\>\<text\>/ig;
    }

    if ($line =~ m/$sh\s+/i) {
      $line =~ s/((?:SOCIAL\s+HISTORY|HABITS))(\s+[a-z]+)/\<\/text\>\<\/section\>\<section ID=\'40\' type='SOCIAL HISTORY'\>\<title\>$1\<\/title\>\<text\>$2/;
    }

    if ($line =~ m/($med1)/i) {
        $line =~ s/(($lh_temp_mod)?($med1)($rh_temp_mod)?($BE|$INCLUDE)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'28\' type='MEDICATION'\>\<title\>$1\<\/title\>\<text\>/ig;
        $line =~ s/(^(?:$lh_temp_mod)?(?:$med1)(?:$rh_temp_mod)?(?:$BE|$INCLUDE)?\s*)(\n)/\<\/text\>\<\/section\>\<section ID=\'28\' type='MEDICATION'\>\<title\>$1\<\/title\>$2\<text\>/im;

    }

    if ($line =~ m/($med2)/i) {
        $line =~ s/(($lh_temp_mod)($med2)($rh_temp_mod)?($BE|$INCLUDE)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'28\' type='MEDICATION'\>\<title\>$1\<\/title\>\<text\>/ig;
        $line =~ s/(^($med2)($rh_temp_mod)?($BE|$INCLUDE)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'28\' type='MEDICATION'\>\<title\>$1\<\/title\>\<text\>/im;
    }

    if ($line =~ m/($med\s+(?:up)?on\s+transfer\s+from)/i) {
        $line =~ s/(($lh_temp_mod)?($med)\s+on\s+transfer\s+from(\s+[A-Z]+)+($BE|$INCLUDE)?\s*\s*\:)/\<\/text\>\<\/section\>\<section ID=\'28\' type='MEDICATION'\>\<title\>$1\<\/title\>\<text\>/ig;
#       print "$1";
    }
#    if ($line =~ m/($med)($rh_temp_mod)($BE|$INCLUDE)\s/i) {
#    print "matched med BE/INCLUDE: $1$2$3";
#        $line =~ s/(($med)($rh_temp_mod))(($BE|$INCLUDE)\s+)/\<\/text\>\<\/section\>\<section ID=\'28\' type='MEDICATION'\>\<title\>$1\<\/title\>\<text\>$4/ig;
#       }

    if ($line =~ m/(MEDICATIONS($rh_temp_mod)?\s+[a-z]+)/) {
        $line =~ s/(MEDICATIONS($rh_temp_mod)?)(\s+[a-z]+)/\<\/text\>\<\/section\>\<section ID=\'28\' type='MEDICATION'\>\<title\>$1\<\/title\>\<text\>$3/g;
    }



   if ($line =~ m/($alg1)|($alg2)/i) {
      $line =~ s/((?:(?:$alg1)|(?:$alg2))\s*\:)/\<\/text\>\<\/section\>\<section ID=\'01\' type='ALLERGY'\>\<title\>$1\<\/title\>\<text\>/ig;
        }

    if ($line =~ m/$hosp/i) {
      $line =~ s/(($lh_org_mod)?($hosp)($rh_org_mod)?(\s*\:|\*+|\-+))/\<\/text\>\<\/section\>\<section ID=\'24\' type='HOSPITAL COURSE'\>\<title\>$1\<\/title\>\<text\>/ig;
      $line =~ s/((?:$lh_org_mod)?(?:$hosp)(?:$rh_org_mod)?)(\s*\n)/\<\/text\>\<\/section\>\<section ID=\'24\' type='HOSPITAL COURSE'\>\<title\>$1\<\/title\>$2\<text\>/i;
      $line =~ s/(\*+($hosp\s))/\<\/text\>\<\/section\>\<section ID=\'24\' type='HOSPITAL COURSE'\>\<title\>$1\<\/title\>\<text\>/ig;
      $line =~ s/(($hosp\s*)($rh_org_mod)\s+([a-z]+|\())/\<\/text\>\<\/section\>\<section ID=\'24\' type='HOSPITAL COURSE'\>\<title\>$1\<\/title\>\<text\>/ig;
      }

#    if ($line =~ m/$hosp/i) {
#      $line =~ s/(($lh_org_mod)?($hosp)($rh_org_mod)?(\s*\:|\*+))/\<\/text\>\<\/section\>\<section ID=\'24\' type='HOSPITAL COURSE'\>\<title\>$1\<\/title\>\<text\>/ig;
#    }
    if ($line =~ m/$ed/i) {
      $line =~ s/(($lh_org_mod)?($ed)($rh_org_mod)?(\s*\:|\*+))/\<\/text\>\<\/section\>\<section ID=\'19\' type='EMERGENCY DEPARTMENT COURSE'\>\<title\>$1\<\/title\>\<text\>/ig;
    }


    if ($line =~ m/$ros/i) {
      $line =~ s/(($ros)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'37\' type='REVIEW OF SYSTEMS'\>\<title\>$1\<\/title\>\<text\>/i;
    }


    if ($line =~ m/$ros\s+/i) {
      $line =~ s/(REVIEW\s+OF\s+SYSTEMS)(\s+[a-z]+)/\<\/text\>\<\/section\>\<section ID=\'37\' type='REVIEW OF SYSTEMS'\>\<title\>$1\<\/title\>\<text\>$2/;
    }
    if ($line =~ m/\s+(?:$phe|$status)(?:\s*\:|\-[^a-z]|($rh_temp_mod)|($rh_loc_mod)|\s+vs)/i) {
      $line =~ s/((?:$lh_org_mod)?(?:$lh_temp_mod)?(?:$phe|$status)(?:$rh_temp_mod)?(?:$rh_loc_mod)?(?:\s*\:|\-[^a-z]))/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>\<text\>/ig;
      $line =~ s/((?:$lh_org_mod)?(?:$lh_temp_mod)?(?:$phe|$status)(?:$rh_temp_mod)?(?:$rh_loc_mod)?)((\s+vss?)(?:\s*\:|\-[^a-z]))/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>\<text\>$2/ig;
    }
    if ($line =~ m/(?:$lh_temp_mod)exam/i) {
       $line =~ s/(($lh_temp_mod)(?:exam(?:ination)?)(?:\s*\:))/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>\<text\>/ig; 
    }
#    elsif ($line =~ m/exams?\s+(?:w+\s+)?reveal/i) {
#      $line =~ s/(exams?\s+(?:w+\s+)?reveal)/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>\<\/title\>\<text\>$1/ig;
#    }

#    elsif ($line =~ m/\s+(?:$phe|$status)\n/i) {
#      $line =~ s/((?:$lh_org_mod)?(?:$lh_temp_mod)?(?:$phe|$status))(\n)/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>$2\<text\>/ig;
#    }
    elsif ($line =~ m/^(?:$lh_org_mod)?(?:$lh_temp_mod)?(?:$phe|$status)\n/im) {
      $line =~ s/^((?:$lh_org_mod)?(?:$lh_temp_mod)?(?:$phe|$status))(\n)/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>$2\<text\>/im;
    }    elsif ($line =~ m/[\_\*\-]\s+exam/i) {
      $line =~ s/([\_\*\-]\s+)((exam)(\s*\:))/$1\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$2\<\/title\>\<text\>/ig;
     }
    elsif ($line =~ m/\:\s*vs[\:\s]/i) {
      $line =~ s/((?:$lh_temp_mod)?(?:status|exam)\s*\:)(\s+vs[\s\:])/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>\<text\>$2/ig;
     }

#    unless ($line =~ m/($phe|$status|exam|physical)/) {
#      if ($line =~ m/($vital)/i) {
#print "Found Vitalsigns: $1";
#         $line =~ s/(($vital)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>\<\/title\>\<text\>$1/i;
#      }
#    }

#    if ($line =~ m/$status/i) {
#      $line =~ s/(($status)(\s*\:|\*))/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>\<text\>/ig; 
#    }
#    elsif ($line =~ m/exam/i) {
#      $line =~ s/(\-+|\*+)\s*(exam\s*\:)/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>\<text\>/ig; 
#    }

    if ($line =~ m/(?:$lab)|(?:tst)?/i) {
      $line =~ s/((?:($lh_temp_mod)|(?:$lh_org_mod))?($lab|$tst)($rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'27\' type='LABORATORY\/STUDIES'\>\<title\>$1\<\/title\>\<text\>/ig;
    }

    if ($line =~ m/$rslt\s*\:/i) {
      $line =~ s/((?:$rslt)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='EXAM\/LABORATORY RESULTS'\>\<title\>$1\<\/title\>\<text\>/ig;
    }
    elsif ($line =~ m/$rslt\s*\n/i) {
      $line =~ s/((?:$rslt)\s*)(\n)/\<\/text\>\<\/section\>\<section ID=\'\' type='EXAM\/LABORATORY RESULTS'\>\<title\>$1\<\/title\>$2\<text\>/ig;
    }




    if ($line =~ m/data/i) {
      $line =~ s/((?:$lh_temp_mod)\s*data(?:$rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'27\' type='LABORATORY\/STUDIES'\>\<title\>$1\<\/title\>\<text\>/ig; 
    }
     if ($line =~ m/data/i) {
      $line =~ s/((?:\-+|\*+|\_+)\s*)(data(?:$rh_temp_mod)?\s*\:)/$1\<\/text\>\<\/section\>\<section ID=\'27\' type='LABORATORY\/STUDIES'\>\<title\>$2\<\/title\>\<text\>/ig; 
    }

    if ($line =~ m/exam/i) {
      $line =~ s/((?:\-+|\*+|\_+|(?:$lh_org_mod))\s*(?:exam(?:ination)?(?:\s+data)?(?:$rh_temp_mod)\s*\:))([^\<])/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>\<text\>$2/ig; 
    }


    if ($line =~ m/$clfnd/i) {
      $line =~ s/(($clfnd)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'07\' type='CLINICAL FINDINGS'\>\<title\>$1\<\/title\>\<text\>/gi;
    }

    if ($line =~ m/$rad/i) {
      $line =~ s/(($rad)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'35\' type='RADIOLOGY'\>\<title\>$1\<\/title\>\<text\>/i;
    }
    if ($line =~ m/$asp/i) {
      $line =~ s/(($lh_temp_mod|$lh_org_mod)?($asp)($rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'04\' type='ASSESSMENT\/PLAN'\>\<title\>$1\<\/title\>\<text\>/ig;
    }
    else {
       if ($line =~ m/$asm/i) {
         $line =~ s/(($lh_temp_mod|$lh_org_mod)?($asm)($rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'03\' type='ASSESSMENT'\>\<title\>$1\<\/title\>\<text\>/ig;
       }

       if ($line =~ m/$pln/i)  {
          unless (($line =~ m/$dspl/i) || ($line =~ m/$tdpln/i) || ($line =~ m/$imppln/i)) {
            $line =~ s/(($lh_temp_mod)?($pln)(?:\s*\:[^\d]|\s*\*))/\<\/text\>\<\/section\>\<section ID=\'33\' type='PLAN'\>\<title\>$1\<\/title\>\<text\>/i;
            $line =~ s/((?:$lh_temp_mod)?(?:$pln))(\s*\n)/\<\/text\>\<\/section\>\<section ID=\'33\' type='PLAN'\>\<title\>$1\<\/title\>$2\<text\>/i;
          }
       }
    }

    if ($line =~ m/$dspl/i) {
         $line =~ s/(($dspl)($rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'18\' type='DISPOSITION\/PLAN'\>\<title\>$1\<\/title\>\<text\>/ig;
       }

    elsif ($line =~ m/$dsp/i) {
         $line =~ s/(($lh_temp_mod)?($dsp)($rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'17\' type='DISPOSITION'\>\<title\>$1\<\/title\>\<text\>/ig;
    }

    if ($line =~ m/$tdpln/i) {
      $line =~ s/(($tdpln)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'41\' type='TO DO\/PLAN'\>\<title\>$1\<\/title\>\<text\>/ig;
    }


    if ($line =~ m/(?:$imppln)\s*\:/i) {
         $line =~ s/((?:$imppln)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'18\' type='IMPRESSION\/PLAN'\>\<title\>$1\<\/title\>\<text\>/ig;
    }

    elsif ($line =~ m/(?:$imppln)\s*\n/i) {
         $line =~ s/($imppln\s*)(\n)/\<\/text\>\<\/section\>\<section ID=\'18\' type='IMPRESSION\/PLAN'\>\<title\>$1\<\/title\>$2\<text\>/ig;
    }


    elsif ($line =~ m/$imp/i) {
      $line =~ s/(($imp)($rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'25\' type='IMPRESSION'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$cnd/i) {
      $line =~ s/(($lh_temp_mod)?($cnd)($rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'11\' type='CONDITION'\>\<title\>$1\<\/title\>\<text\>/ig;
    }

    if ($line =~ m/$cmp/i) {
      $line =~ s/(($cmp)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'10\' type='COMPLICATIONS'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$cod/i) {
      $line =~ s/(($cod)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'08\' type='CODE STATUS'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$appt/i) {
      $line =~ s/(($lh_temp_mod|$lh_org_mod)?($appt)\s*(?:\(\s+s\s+\))?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'02\' type='APPOINTMENTS'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$diag2/i) {
       $line =~ s/((?:$diag2)\s?)/\<\/text\>\<\/section\>\<section ID=\'13\' type='DIAGNOSES'\>\<title\>$1\<\/title\>\<text\>/ig;
     }

    if ($line =~ m/$diag3/i) {
      $line =~ s/(($diag3)\s?)/\<\/text\>\<\/section\>\<section ID=\'13\' type='DIAGNOSES'\>\<title\>$1\<\/title\>\<text\>/ig;
    }

#    if ($line =~ m/$diag/i) {
#      $line =~ s/(($diag)($rh_temp_mod)?\s*[\:\;])/\<\/text\>\<\/section\>\<section ID=\'13\' type='DIAGNOSES'\>\<title\>$1\<\/title\>\<text\>/ig;
#    }

    if ($line =~ m/$diag/i) {
      $line =~ s/(($diag)($rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'13\' type='DIAGNOSES'\>\<title\>$1\<\/title\>\<text\>/ig;
    }



    if ($line =~ m/$prob/i) {
      $line =~ s/(($prob)($rh_org_mod)?\s*[\:\;\*+])/\<\/text\>\<\/section\>\<section ID=\'34\' type='PROBLEMS'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$treat3/i) {
      $line =~ s/(($treat3)\s*\:?)/\<\/text\>\<\/section\>\<section ID=\'42\' type='TREATMENT'\>\<title\>$1\<\/title\>\<text\>/i;g
    }
    if ($line =~ m/$treat2|$treat1/i) {
      $line =~ s/((($treat2)|($treat1))\s*\:?)/\<\/text\>\<\/section\>\<section ID=\'42\' type='OTHER TREATMENTS\/PROCEDURES'\>\<title\>$1\<\/title\>\<text\>/i;g
    }

    if ($line =~ m/$proc1|$proc2|$proc3/i) {
      $line =~ s/(($proc1|$proc2|$proc3)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'29\' type='OPERATIONS\/PROCEDURES'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$proc4/i) {
      $line =~ s/(($proc4)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'29\' type='OPERATIONS\/PROCEDURES'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$proc5/i) {
      $line =~ s/(($proc5)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'29\' type='\/PROCEDURE DESCRIPTION'\>\<title\>$1\<\/title\>\<text\>/ig;
    }

    if ($line =~ m/$pp_proc_diag/i) {
      $line =~ s/(($pp_proc_diag)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'29\' type='POSTPARTUM DIAGNOSTIC PROCEDURES'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$pp_proc_ther/i) {
      $line =~ s/(($pp_proc_ther)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'29\' type='POSTPARTUM THERAPEUTIC PROCEDURES'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$pp_proc_oth/i) {
      $line =~ s/(($pp_proc_oth)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'42\' type='OTHER POSTPARTUM TREATMENT'\>\<title\>$1\<\/title\>\<text\>/i;
    }



    if ($line =~ m/$dis_wnd/i) {
      $line =~ s/(($dis_wnd)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'16\' type='DISCHARGE WOUND CARE'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$dis_fol1|$dis_fol2|$dis_fol3|$follup/i) {
      $line =~ s/((?:$dis_fol1|$dis_fol2|$dis_fol3|$follup)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'21\' type='FOLLOW-UP'\>\<title\>$1\<\/title\>\<text\>/i;
      $line =~ s/((?:$dis_fol1)|(?:$dis_fol2)|(?:$dis_fol3)|(?:$follup)\s*)(\n)/\<\/text\>\<\/section\>\<section ID=\'21\' type='FOLLOW-UP'\>\<title\>$1\<\/title\>$2\<text\>/i;
    }

#   if ($line =~ m/$follup/i) {
#      $line =~ s/(($follup)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'21\' type='FOLLOW-UP'\>\<title\>$1\<\/title\>\<text\>/i;
#    }


    if ($line =~ m/$dis_act\s*\:/i) {
      $line =~ s/(($dis_act)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'14\' type='DISCHARGE ACTIVITY'\>\<title\>$1\<\/title\>\<text\>/i;
    }
    elsif ($line =~ m/^$dis_act\s*\n/im) {
      $line =~ s/(^(?:$dis_act)\s*)(\n)/\<\/text\>\<\/section\>\<section ID=\'14\' type='DISCHARGE ACTIVITY'\>\<title\>$1\<\/title\>$2\<text\>/im;
    }   

    if ($line =~ m/$dis_ord/i) {
      $line =~ s/(($dis_ord)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='DISCHARGE ORDERS'\>\<title\>$1\<\/title\>\<text\>/i;
    }


    if ($line =~ m/$dict1\s*\:/i) {
      $line =~ s/($dict1\s*\:)/\<\/text\>\<\/section\>\<section ID=\'39\' type='DICTATION\/AUTHOR DETAILS'\>\<title\>$1\<\/title\>\<text\>/ig;
    }
#    elsif ($line =~ m/$dict/i) {
#      $line =~ s/($dict[^\s*\:]+)/\<\/text\>\<\/section\>\<section ID=\'39\' type='SIGNATURE'\>\<title\>\<\/title\>\<text\>$1/ig;
#    }

    if ($line =~ m/$dict2\s*\:/) {
      $line =~ s/($dict2\s*\:)/\<\/text\>\<\/section\>\<section ID=\'39\' type='DICTATION\/AUTHOR DETAILS'\>\<title\>\<\/title\>\<text\>$1/;
    }

    if ($line =~ m/$dict3\s/) {
      $line =~ s/($dict3\s+[A-Z])/\<\/text\>\<\/section\>\<section ID=\'39\' type='DICTATION\/AUTHOR DETAILS'\>\<title\>\<\/title\>\<text\>$1/;
    }


    if ($line =~ m/$cons1/i) {
      $line =~ s/(($cons1)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'12\' type='CONSULTANTS'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$cons2/i) {
      $line =~ s/(($cons2)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='CONSULTATION SUMMARY'\>\<title\>$1\<\/title\>\<text\>/i;
    }


    if ($line =~ m/^$hst\s*\n/im) {
      $line =~ s/(^(?:$hst)\s*)(\n)/\<\/text\>\<\/section\>\<section ID=\'14\' type='HEALTH STATUS'\>\<title\>$1\<\/title\>$2\<text\>/im;
    }   


    if ($line =~ m/$cmt/i) {
      $line =~ s/(($cmt)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'09\' type='COMMENTS'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$inst($rh_temp_mod)?\s*\:/i) {
        $line =~ s/(($lh_temp_mod|$lh_org_mod)+($inst)($rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'26\' type='INSTRUCTIONS'\>\<title\>$1\<\/title\>\<text\>/ig;
    }
    if ($line =~ m/^$inst\s*\n/im) {
        $line =~ s/(^(?:$inst)(?:$rh_temp_mod)?\s*)(\n)/\<\/text\>\<\/section\>\<section ID=\'26\' type='INSTRUCTIONS'\>\<title\>$1\<\/title\>$2\<text\>/im;
    }


    if ($line =~ m/$evnt/i) {
      $line =~ s/(($evnt)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'43\' type='EVENTS'\>\<title\>$1\<\/title\>\<text\>/i;
    }

    if ($line =~ m/$ddetails/i) {
      $line =~ s/(($ddetails)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'44\' type='DISCHARGE DETAILS'\>\<title\>$1\<\/title\>\<text\>/i;

    }

    if ($line =~ m/$rptsts/i) {
      $line =~ s/(($rptsts)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'\' type='REPORT STATUS'\>\<title\>$1\<\/title\>\<text\>/i;

    }

    if ($line =~ m/$dis_notif/i) {
      $line =~ s/($dis_notif)(\s+)/\<\/text\>\<\/section\>\<section ID=\'\' type='DISCHARGE NOTIFICATION SUMMARY'\>\<title\>$1\<\/title\>$2\<text\>/i;

    }

    if ($line =~ m/$prelim/i) {
      $line =~ s/($prelim)(\s+)/\<\/text\>\<\/section\>\<section ID=\'\' type='PRELIMINARY REPORT CONTENT'\>\<title\>$1\<\/title\>$2\<text\>/i;

    }


    if ($line =~ m/$pat_info/i) {
      $line =~ s/(($pat_info))/\<\/text\>\<\/section\>\<section ID=\'45\' type='PATIENT INFO'\>\<title\>$1\<\/title\>\<text\>/i;

    }


    if ($line =~ m/$diet/i) {
      $line =~ s/(($lh_temp_mod)?($diet)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'46\' type='DIET'\>\<title\>$1\<\/title\>\<text\>/ig;

    }


    if ($line =~ m/$work/i) {
      $line =~ s/(($work)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'47\' type='WORK RETURN'\>\<title\>$1\<\/title\>\<text\>/i;

    }


    if ($line =~ m/$addend/i) {
      $line =~ s/(($addend)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'49\' type='ADDENDUM'\>\<title\>$1\<\/title\>\<text\>/ig;

    }
    if ($line =~ m/$prevcard/i) {
      $line =~ s/(($prevcard)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'50\' type='PREVIOUS CARDIOVASCULAR INTERVENTIONS'\>\<title\>$1\<\/title\>\<text\>/i;

    }

    if ($line =~ m/$preopst/i) {
      $line =~ s/(($preopst)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'51\' type='PREOPERATIVE STATUS'\>\<title\>$1\<\/title\>\<text\>/ig;

    }

    if ($line =~ m/$trans\s*\:/i) {
      $line =~ s/(($trans)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'53\' type='TRANSCRIPTION DETAILS'\>\<title\>$1\<\/title\>\<text\>/ig;
        }

    if ($line =~ m/$pch\s*\:/i) {
      $line =~ s/(($pch)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'54\' type='PAST CARDIAC HISTORY'\>\<title\>$1\<\/title\>\<text\>/ig;
        }

#########################################################################################################################################################
##                         Identify Progress Note or Discharge Summary sections that have no headings:
#
# Use non-greedy (i.e., minimal matching) quanitifers ( +? ) in these substitution patterns.  With longest match (default), we often start the
# new section too late because a sentence further along in the section also matches the pattern.  We want to start the new section at the first match
# after the metadata section,  and this requires a non-gready match

#########################################################################################################################################################
    if ($line =~ m/$section_intro1/i) {     
       $line =~ s/(\<section\s+ID\=\'\d*\'\s+type\=\'(?:(?:ATTENDING\s+MD)|(?:ADMISSION|DISCHARGE|EVALUATION)\s+DATE|DATE\s+OF\s+EXPIRATION|CONSULTANTS)\'\>\<title\>(?:[^\<])+\<\/title\>(?:\n)?\<text\>[^<]+?)(\n)($section_intro1)/$1\<\/text\>\<\/section\>$2\<section ID=\'\' type='MANAGEMENT SUMMARY'\>\<title\>\<\/title\>\<text\>$3/i;
    }

    elsif ($line =~ m/$section_intro2/i) { 
#print "matched second pattern. Line is $line\n";        
        $line =~ s/(\<section\s+ID\=\'\d*\'\s+type\=\'(?:(?:ATTENDING\s+MD)|(?:ADMISSION|DISCHARGE|EVALUATION)\s+DATE|DATE\s+OF\s+EXPIRATION|CONSULTANTS)\'\>\<title\>(?:[^\<])+\<\/title\>(?:\n)?\<text\>[^<]+?)(\n)($section_intro2)/$1\<\/text\>\<\/section\>$2\<section ID=\'\' type='MANAGEMENT SUMMARY'\>\<title\>\<\/title\>\<text\>$3/i;
     }
    elsif ($line =~ m/$section_intro3/i) {     
       $line =~ s/(\<section\s+ID\=\'\d*\'\s+type\=\'(?:(?:ATTENDING\s+MD)|(?:ADMISSION|DISCHARGE|EVALUATION)\s+DATE|DATE\s+OF\s+EXPIRATION|CONSULTANTS)\'\>\<title\>(?:[^\<])+\<\/title\>(?:\n)?\<text\>[^<]+?)(\n)($section_intro3)/$1\<\/text\>\<\/section\>$2\<section ID=\'\' type='PROGRESS SUMMARY'\>\<title\>\<\/title\>\<text\>$3/i;
    }

    elsif ($line =~ m/$section_intro4/i) {     
       $line =~ s/(\<section\s+ID\=\'\d*\'\s+type\=\'(?:(?:ATTENDING\s+MD)|(?:ADMISSION|DISCHARGE|EVALUATION)\s+DATE|DATE\s+OF\s+EXPIRATION|CONSULTANTS)\'\>\<title\>(?:[^\<])+\<\/title\>(?:\n)?\<text\>[^<]+?)(\n)($section_intro4)/$1\<\/text\>\<\/section\>$2\<section ID=\'\' type='PROGRESS SUMMARY'\>\<title\>\<\/title\>\<text\>$3/i;
    }



# Look for list keywords and add list structure
#     $line =~s/(number\s(1|one))/\n\<list\>\n\<list_item\>\<L$2\>$1\<\/L$2\>/ig;
#
#     $line =~ s/(number\s(2|3|4|5|6|7|8|9|10|two|three|four|five|six|seven|eight|nine|ten))/\<\/list_item\>\n\<list_item\>\<L$2\>$1\<\/L$2\>/ig;
#
#     $line =~ s/(next\s+number|number\s+next)/\<\/list_item\>\n\<list_item\>\<Lnext\>$1<\/Lnext\>/ig;



#   Add closing  </section>  tag

#    s/(\<\/text\>)(\/doc\>)/$1\<\/section\>$2/;

# close last section
 $line =~  s/(\<\/doc\>)/\<\/text\>\n\<\/section\>$1/ig;

    print OUTPUT "$line";

#} # WHILE

close INPUT;
close OUTPUT;

# Third pass through medical record. Find subsections.  Find report type.
#print "Finding subheadings and adding subsection structure (No subsections for Mayo)\n";

$input_file = "$file_name" . ".out2";
$output_file = "$file_name" . ".out3";

open INPUT, "$input_file" or die "Cannot open input file $input_file\n";
open OUTPUT, ">$output_file" or die "Cannot open output file $output_file\n";


$section = '';
$subsection = '';


 undef $/;
 $line = <INPUT>;

#while ($line = <INPUT>) {


# Add closing <list_item> tags
# $line =~ s/(L\d+\>[^\<]+)(\<\/text\>)/$1\<\/list_item\>\<\/list\>$2/ig;
# $line =~ s/(Lnext\>[^\<]+)(\<\/text\>)/$1\<\/list_item\>\<\/list\>$2/ig;
# Remove extraneous list_item
 # if ($line =~ m/(\<text.+)\<\/list_item\>/) {
 # print "\nExtra list_item tag\n";
 #  $line =~ s/(text.+)\<\/list_item\>/$1/i;
#  }
# Add list enumerator tags
# $line =~ s/\<(\/)?Lone/\<$1L1/ig;
# $line =~ s/\<(\/)?Ltwo/\<$1L2/ig;
# $line =~ s/\<(\/)?Lthree/\<$1L3/ig;
# $line =~ s/\<Lfour/\<L4/i;
# $line =~ s/\<Lfive/\<L5/i;
# $line =~ s/\<Lsix/\<L6/i;
# $line =~ s/\<Lseven/\<L7/i;
# $line =~ s/\<Leight/\<L8/i;
# $line =~ s/\<Lnine/\<L9/i;
# $line =~ s/\<Lten/\<L10/i;



# Add sequential section IDs    --  Replace with unique section-type identifier for i2b2 challenge
#  if ($line =~m/\<section/) {
#    $line =~ s/(\<section)/$1 ID=\'$k\'/i;
#    $k++;
#  }

#The followign logic  assumes section labels are on different lines of the input.  This should generally be true but may not always be. 
  if ($line =~ m/\<section\s+ID\=\'\d+\'\s+type\=\'([^\']+)\'/) {
    $section = $1;

   }


# Identify missed Physical Exam in sections where they don't belong
    if (($section =~ m/MEDICATION/) || ($section =~ m/HISTORY/ ||($section =~ m/ALLERGY/)) ) {

    if ($line =~ m/(exam|$vital)\s*\:/i) {
        $line =~ s/(exam\s*\:)/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>\<text\>/i;
        $line =~ s/(($vital)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>\<\/title\>\<text\>$1/i;
        $section = 'PHYSICAL EXAMINATION';
     }
    }

    if ($line =~ m/PE\s+T[^a-z,A-Z]/) {
                $line =~ s/(PE)(\s+T[^a-z,A-Z])/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINATION'\>\<title\>$1\<\/title\>\<text\>$2/i;
    }

    if (($section =~ m/PAST MEDICAL HISTORY/)|| ($section =~ m/MEDICATION/) ) {
        $line =~ s/((?:admission|admit|daily\s+)?status\s*\:)/\<\/text\>\<\/section\>\<section ID=\'32\' type='PHYSICAL EXAMINIATION'\>\<title\>$1\<\/title\>\<text\>/i;
    }


# Identify CARBON COPY section
    if (($section =~ m/TRANSCRIPTION/) || ($section =~ m/SIGNATURE/) ) {
        $line =~ s/($cc\s*\:)/\<\/text\>\<\/section\>\<section ID=\'52\' type='CARBON COPY'\>\<title\>$1\<\/title\>\<text\>/ig;
    }


# Identify INSTRUCTIONS outside of MEDICATIONS (Instructions: can be a subsection of MEDICATION.) 

#    unless (($section =~ m/MEDICATION/) || ($section =~ m/INSTRUCTION/) ) {
#      if ($line =~ m/$inst/i) {
#        $line =~ s/(($lh_temp_mod|$lh_org_mod)?($inst)($rh_temp_mod)?\s*\:)/\<\/text\>\<\/section\>\<section ID=\'26\' type='INSTRUCTIONS'\>\<title\>$1\<\/title\>\<text\>/ig;
#      }
#    }

# Identify ADMISSION DATE outside of HEADER  (admission date is usually part of header, which is already tagged in 2006 i2b2 documents
#    unless ($section =~ m/HEADER/i) {    
#      if ($line =~ m/$admitdate/i) {
#        $line =~ s/(($admitdate)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'48\' type='ADMISSION DATE'\>\<title\>$1\<\/title\>\<text\>/ig;
#      }
#    }

   # ADMISSION DATE outside of header in 2010 i2b2 documents (Partners and Beth Israel)

      if ($line =~ m/^$admitdate/im) {
        $line =~ s/((^$admitdate)\s*\:)/\<section ID=\'48\' type='ADMISSION DATE'\>\<title\>$1\<\/title\>\<text\>/im;
      }
      elsif ($line =~ m/$admitdate/i) {
        $line =~ s/(($admitdate)\s*\:)/\<\/text\>\<\/section\>\<section ID=\'48\' type='ADMISSION DATE'\>\<title\>$1\<\/title\>\<text\>/ig;
      }




#####################################################################Subsection code commented out for Mayo
## Identify subsections in REVIEW of SYSTEMS and PHYSICAL EXAM
#
#  if ($section eq 'PHYSICAL EXAMINATION') {

    if ($line =~ m/$vital(\:|\s*-)/i) {
    print "\nFound vital signs\n";
      $line =~ s/(($vital)(\s*\:|\s*-))/\<\/text\>\<\/subsection\>\<subsection type='Vital Signs'\>\<title\>$1\<\/title\>\<text\>/i;
    }
#    elsif ($line =~ m/^\<text>\s+T\s*[1,9].+RA/) {
##print "\nFound vital signs\n";
#       $line =~ s/(^\<text\>)/$1\<subsection type='Vital Signs'\>\<title\>\<\/title\>\<text\>/;
#    }
#
#   }
#
#
   if (($section eq 'REVIEW OF SYSTEMS') || ($section eq 'PHYSICAL EXAMINATION') || ($section eq 'HOSPITAL COURSE')||($section eq 'EMERGENCY DEPARTMENT COURSE')|| ($section =~m/ASSESSMENT/)) {

     if ($line =~ m/$gen/i) {
     $line =~ s/((?:\s(?:[1][0-2]|\d)\.\s+)?($gen)\s*(\:|\s*\-))/\<\/text\>\<\/subsection\>\<subsection type='General'\>\<title\>$1\<\/title\>\<text\>/i;
#
     }

#     $line =~ s/(RA\s*[\.\,]?\s+)($gen)(\s+)/$1\<subsection type='General'\>\<title\>$2\<\/title\>$3\<text\>/i;
#


  }
#     else {
#     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($neck)(\s+exam)?\:)/\<\/text\>\<\/subsection\>\<subsection type='Neck'\>\<title\>$1\<\/title\>\<text\>/i;
#     }
#
#Some report have HEENT and NECK subsections
     if ($line =~ m/$heent/i) {
     $line =~ s/((?:\s(?:[1][0-2]|\d)\.\s+)?($heent)(\s+exam)?\s*(?:\;|\:))/\<\/text\>\<\/subsection\>\<subsection type='HEENT'\>\<title\>$1\<\/title\>\<text\>/i;
     }

     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($neck)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Neck'\>\<title\>$1\<\/title\>\<text\>/i;

     $line =~ s/((?:\s(?:[1][0-2]|\d)\.\s+)?($neuro)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Neurologic'\>\<title\>$1\<\/title\>\<text\>/i;

     $line =~ s/((?:\s(?:[1][0-2]|\d)\.\s+)?($cardiov)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Cardiovascular'\>\<title\>$1\<\/title\>\<text\>/i;

     $line =~ s/((?:\s(?:[1][0-2]|\d)\.\s+)?($coron)(\s+exam)?\\s*:)/\<\/text\>\<\/subsection\>\<subsection type='Coronary'\>\<title\>$1\<\/title\>\<text\>/i;

     $line =~ s/((?:\s(?:[1][0-2]|\d)\.\s+)?($resp)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Respiratory'\>\<title\>$1\<\/title\>\<text\>/i;
#
     $line =~ s/((?:\s(?:[1][0-2]|\d)\.\s+)?($gi)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='GI'\>\<title\>$1\<\/title\>\<text\>/i;
#
#     $line =~ s/((?:\s(?:[1][0-2]|\d)\.\s+)?($gu)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='GU'\>\<title\>$1\<\/title\>\<text\>/i;
#
     if ($line =~ m/$hemonc\s*\:/) {
       $line =~ s/(\s(?:(?:[1]?[0-2]|\d)\.\s+)?($hemonc)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Hematologic-oncologic'\>\<title\>$1\<\/title\>\<text\>/i;
     }
     else {
      $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($hem)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Hematologic'\>\<title\>$1\<\/title\>\<text\>/i;
      $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($onc)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Oncologic'\>\<title\>$1\<\/title\>\<text\>/i;
     }

     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($musk)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Musculoskeletal'\>\<title\>$1\<\/title\>\<text\>/i;
#
#     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($psych)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Psychiatric'\>\<title\>$1\<\/title\>\<text\>/i;
#
     if ($line =~m/$chstlng/) {
     print "matched chest/lungs\n";
       $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($chstlng)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Chest-Lung'\>\<title\>$1\<\/title\>\<text\>/i;
     }
     else {

       $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($chst)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Chest'\>\<title\>$1\<\/title\>\<text\>/i;

       $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($lng)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Lungs'\>\<title\>$1\<\/title\>\<text\>/i;

     }
     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($abdo)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Abdomen'\>\<title\>$1\<\/title\>\<text\>/i;

     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($heart)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Heart'\>\<title\>$1\<\/title\>\<text\>/i;

     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($extr)(\s+exam)?\s+\:)/\<\/text\>\<\/subsection\>\<subsection type='Extremities'\>\<title\>$1\<\/title\>\<text\>/i;

     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($skin)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Skin'\>\<title\>$1\<\/title\>\<text\>/i;

     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($cardiac)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Cardiac'\>\<title\>$1\<\/title\>\<text\>/i;
#
     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($mst)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Mental Status'\>\<title\>$1\<\/title\>\<text\>/i;
##     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($pain)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Pain'\>\<title\>$1\<\/title\>\<text\>/i;
#
#     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($proph)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Prophylaxis'\>\<title\>$1\<\/title\>\<text\>/i;
#
     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($pulm)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Pulmonary'\>\<title\>$1\<\/title\>\<text\>/i;
#
     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($rect)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Rectal'\>\<title\>$1\<\/title\>\<text\>/i;
#
#     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($rheum)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Rheumatologic'\>\<title\>$1\<\/title\>\<text\>/i;
#
#     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($ortho)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Orthopedics'\>\<title\>$1\<\/title\>\<text\>/i;
#
#     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($gyn)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Gynecology'\>\<title\>$1\<\/title\>\<text\>/i;
#     if ( $line =~ m/(($rnlfen)\:)/) {
#
#       $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($rnlfen)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Renal\/FEN'\>\<title\>$1\<\/title\>\<text\>/i;
#
#     }
##
#     else {
#
#       $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($rnl)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Renal'\>\<title\>$1\<\/title\>\<text\>/i;
#
#
#       if  ( $line =~ m/(($fen)\:)/) {
#          $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($fen)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='FEN'\>\<title\>$1\<\/title\>\<text\>/i;
#       }
#       else {
#
#       $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($nutr)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Nutrition'\>\<title\>$1\<\/title\>\<text\>/i;
#       }
#     }
##
     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($endo)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Endocrine'\>\<title\>$1\<\/title\>\<text\>/i;
#
     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($inf1)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Infectious Disease'\>\<title\>$1\<\/title\>\<text\>/i;
#     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($inf2)(\s+exam)?\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Infectious Disease'\>\<title\>$1\<\/title\>\<text\>/i;
#
#
#     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($cc)\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Chief Complaint'\>\<title\>$1\<\/title\>\<text\>/i;
#
#     $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($dispo)\s*\:)/\<\/text\>\<\/subsection\>\<subsection type='Disposition'\>\<title\>$1\<\/title\>\<text\>/i;
#
#}
#
#
#
#  if (($section eq 'HOSPITAL COURSE')||($section eq 'ASSESSMENT\/PLAN') || ($section eq 'IMPRESSION')) {
#     if ( $line =~m/$idx/i) {
#         $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($idx)\:)/\<\/text\>\<\/subsection\>\<subsection type='ID Diagnosis'\>\<title\>$1\<\/title\>\<text\>/i;
#     }
#     elsif ($line =~ m/$ddx/i) {
#         $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($ddx)\:)/\<\/text\>\<\/subsection\>\<subsection type='Differential Diagnosis'\>\<title\>$1\<\/title\>\<text\>/i;
#     }
#     elsif ($line =~ m/$dx/i) {
#         $line =~ s/((?:\s(?:[1]?[0-2]|\d)\.\s+)?($dx)\:)/\<\/text\>\<\/subsection\>\<subsection type='Diagnosis'\>\<title\>$1\<\/title\>\<text\>/i;
#     }
#
#  }


#    $line = s/(\<\/subsection\>)(\<\/text\>)/$1$2/;

   print OUTPUT "$line";
# }  #WHILE

close INPUT;
close OUTPUT;

# Third pass through medical record.  Check for errors in list enumeration and normalize list item representation.  Augment subsections.
#$input_file = "$file_name" . ".out2";
#$output_file = "$file_name" . ".out3";
#open INPUT, "1397570.out2" or die "Cannot open input file\n";
#open OUTPUT, ">1397570.out3" or die "Cannot open output file\n";

#open INPUT, "$input_file" or die "Cannot open input file $input_file\n";
#open OUTPUT, ">$output_file" or die "Cannot open output file $output_file\n";


#$section = '';
#$subsection = '';


#while ($line = <INPUT>) {

#  $line =~ s/(\<doc)\>/$1 type='$report_type'/;

#  if ($line =~ m/\<section\s+ID\=\'\d+\'\s+type\=\'([^\']+)\'/) {
#    $section = $1;
#    print "\n$section\n";
#   }


#   if ($line =~ m/\<subsection/) {
#      $line =~ s/((\<[^\>]+\>)?)/\<\/subsection\>$1/;
#      $line =~ s/(\<subsection\>)/\<\/subsection\>$1/; 
#   }
#
#  if ($line =~ m/\<subsection\s+type\=\'([^\']+)\'/) {
#    $subsection = $1;
#   }
#   if (($section eq 'REVIEW OF SYSTEMS') || ($section eq 'PHYSICAL EXAMINATION')) {
#    unless ($subsection =~m/vital\ssigns/i) {
#      $line =~ s/($heart\:)(.+)/\<\/subsection\>\<subsection type = 'Heart'\>\<title\>$1\<\/title\>$2/i;
#    }
#   }
#############################################################
#  if ($line =~ m/\<list\>/) {
#    $list_flag = 'true';
#    $item = 0;
#  }
#  elsif ($line =~ m/\<\/list\>/) {
#    $list_flag = 'false';
#    $item = 0;
#  }
# if (($line =~m/Lnext/) && ($list_flag eq 'false')) {
#    print "In section \'$section\', \'Next number\' has no preceding list number.\n\n";
#    $item = 0; 
#    $line =~ s/(\<list_item\>)/\<list\>$1/;
# }
#
#  if ($line =~ m/list_item/) {
#
#    $item++;
#    $line =~ s/Lnext/L$item/ig; 
#
#  }
#  if  (($line =~ m/L(\d+)/) && ($list_flag eq 'true')) {
#     unless ($item == $1) {
#       print "Cannot find list item $item in section \'$section\'";
#       if ($prevline =~m/\s(($item|number)[^\<]+)\</){
#         print "  Is it \'$1\' ?\n\n"; 
#       }
#
#       else {
#        print "\n\n";
#       }
#       $item++;
#    }
#  }
#  if ($line =~ m/Lnext/)  {
#    if (list_flag eq 'true') {
#       $line =~ s/(\<\/L(next)\>[^\<]+)(\<\/text)/$1\<\/list_item\>\<\/list\>$3/;
#    }
#
#  }
#
## Standardize reprentations of enumerators 
#  $line =~ s/number\s+one/Number 1/i;
#  $line =~ s/number\s+two/Number 2/i;
#  $line =~ s/number\s+three/Number 3/i;
#  $line =~ s/number\s+four/Number 4/i;
#  $line =~ s/number\s+five/Number 5/i;
#  $line =~ s/number\s+six/Number 6/i;
#  $line =~ s/number\s+seven/Number 7/i;
#  $line =~ s/number\s+eight/Number 8/i;
#  $line =~ s/number\s+nine/Number 9/i;
#  $line =~ s/number\s+ten/Number 10/i;
#  $line =~ s/\<L(\d+)\>next\s+number/<L$1>Number $1/i;
#  $line =~ s/\<L(\d+)\>number\s+next/<L$1>Number $1/i;
##########################################################################
#
#
# print OUTPUT "$line";
# $prevline = $line;
#
#}
#
#close INPUT;
#close OUTPUT;




# Fourth pass through medical record.  Close subsections.  Ensure case consistency in <text> tags, insert missing section tags,
#  and eliminate spurious (closing </text></subsection> tags introduced at first subsection.


#print "Closing subsections\n";

$input_file = "$file_name" . ".out3";
$output_file = "$file_name" . ".out4";

open INPUT, "$input_file" or die "Cannot open input file $input_file\n";
open OUTPUT, ">$output_file" or die "Cannot open output file $output_file\n";


#$section = '';
#$subsection = '';


 undef $/;
 $line = <INPUT>;

# while ($line = <INPUT>) {


#  if ($line =~ m/^\<subsection/) {
#     if ($line =~ m/\<\/text\>/) { $line =~ s/(^\<subsection.+\<\/text\>)/$1\<\/subsection\>\<\/text\>/; }   
#     else { $line =~ s/(^\<subsection.+)/$1\<\/text\>\<\/subsection\>/; }
#
#  }

#  Add closing test and subsection tags before opening subsection tag.
if ($line =~ m/^\<subsection/) {
    $line =~ s/(^\<subsection)/\<\/text\>\<\/subsection\>$1/;
 }



  $line =~ s/\<TEXT\>/\<section ID=\"\" type= \"HEADER\"\>\<text\>/;
  $line =~ s/\<\/TEXT\>/\<\/text\>\<\/section\>/;

#  Add closing text and section tags to end of document:
      $line =~ s/$/\<\/text\>\<\/section\>/;
#  }

# Move closing text and (sub)section tags to precede carriage return/Line feed:
    $line =~ s/(\n)(\<\/text\>\<\/(?:sub)?section\>)/$2$1/g;
    $line =~ s/(\<text\>)(\n)/$2$1/g;

# Remove extra closing text and subsection tags preceding first subsection:
    $line =~ s/(\<section[^\>]+\>\<title\>[^\<]+\<\/title\>\n?\<text\>[^\<]+)\<\/text\>\<\/subsection\>/$1/g;


#  Add missing  closing subsection tag at end of section:
$line =~ s/(\<subsection[^\>]+\>\<title\>[^\<]+\<\/title\>\n?\<text\>[^\<]+\<\/text\>)(\<\/section\>)/$1\<\/subsection\>$2/g;







#    $line =~s/\&/\&amp\;/g;
#    $line =~s/\'/\&apos\;/g;       
#    $line =~s/\>/\&gt;/g;
#    $line =~s/\</\&lt;/g;
#    $line =~s/\"/\&quot;/g;        


print OUTPUT "$line";

# } # WHILE

close INPUT;
close OUTPUT;

print "Done.\n";
