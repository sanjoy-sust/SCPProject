
var c = new ShellScript();
var hostChallenge = '1122334455667788';
var initialUpdateCommand = '8050000008'+hostChallenge+'0000';
var responseData = c.send(initialUpdateCommand);

//check response Data receive is success or not
c.assertSW('9000');

//session Key Generation
var baseKey  = '404142434445464748494A4B4C4D4E4F4041424344454647';
var sequenceCounter = c.subbytes(responseData,12,2);
var zeroPadding = '000000000000000000000000';
var eightPadding = '8000000000000000';
var derivationDataForSEnc = '0182'+sequenceCounter+zeroPadding;
var derivationDataForCMAC = '0101'+sequenceCounter+zeroPadding;
var icv = '0000000000000000';
var SENC = c.tdes_cbc_icv(derivationDataForSEnc,baseKey,icv);
var CMACKey = c.tdes_cbc_icv(derivationDataForCMAC,baseKey,icv);


//cardCryptogram generation and verify
var cardCryptogram = c.subbytes(responseData,20,8);
var cardChallenge = c.subbytes(responseData,14,6);
var cardCrptogramGenInput = hostChallenge+sequenceCounter+cardChallenge+'8000000000000000';
var cardCryptogramStringGenerated = c.tdes_cbc_icv(cardCrptogramGenInput,SENC,icv);
var cardCryptogramFromTerminal = c.subbytes(cardCryptogramStringGenerated,16,8);

//verify CardCryptogram
c.assertEquals(cardCryptogramFromTerminal,cardCryptogram);

//generatehostCryptogram
var hostCryptogramGenInput = sequenceCounter+cardChallenge+hostChallenge+eightPadding;
var hostCryptogramString =  c.tdes_cbc_icv(hostCryptogramGenInput,SENC,icv);
var hostCryptogram = c.subbytes(hostCryptogramString,16,8);

//generate CMAC
var cmacCommand = '8482000010'+hostCryptogram+'800000';
var toTdes = c.subbytes(cmacCommand,0,8);
var toFTdes = c.subbytes(cmacCommand,8,8);

var singleDesKey = c.subbytes(CMACKey,0,8);
var tripleDesKey = CMACKey+singleDesKey;

var icvForTripleDes = c.des_cbc(toTdes,singleDesKey);

var cmac = c.tdes_cbc_icv(toFTdes,tripleDesKey,icvForTripleDes);
var xternalAuthenticateCommand = '8482000010'+hostCryptogram+cmac+'00';
var responseForxternalCommand = c.send(xternalAuthenticateCommand);
//check success or not
c.assertSW('9000');
c.assertReceive(responseForxternalCommand,'9000');